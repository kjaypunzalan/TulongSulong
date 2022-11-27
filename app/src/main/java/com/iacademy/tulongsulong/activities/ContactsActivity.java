package com.iacademy.tulongsulong.activities;
import com.iacademy.tulongsulong.models.ContactsModel;
import com.iacademy.tulongsulong.adapters.ContactsAdapter;
import com.iacademy.tulongsulong.utils.RecyclerOnItemClickListener;
import com.iacademy.tulongsulong.R;
import android.app.Dialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity implements RecyclerOnItemClickListener {

    //declare variables
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    private RecyclerView rvContactList;
    private ArrayList<ContactsModel> listModels = new ArrayList<>();
    private pl.droidsonroids.gif.GifImageView btnHome;
    private Button btnAddContact;
    private Dialog addContactDialog;
    private Dialog editContactDialog;
    private String name;
    private String email;
    private String number;

    private ImageView ivPhoto;
    private Button btnUpload, btnCamera, btnGallery;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> cameraIntentLauncher, galleryIntentLauncher;
    private int PICK_IMAGE = 100;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        //hide action bar
        getSupportActionBar().hide();

        //instantiate variables
        rvContactList = findViewById(R.id.rv_contactlist);
        btnAddContact = findViewById(R.id.btn_addContact);
        btnHome = findViewById(R.id.btn_home);
        addContactDialog = new Dialog(this);
        editContactDialog = new Dialog(this);



        //FIREBASE
        mAuth = FirebaseAuth.getInstance();

        //Recycler View logic
        rvContactList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvContactList.setAdapter(new ContactsAdapter(listModels, this, this)); //set adaptor that provide child views

        //call void methods
        addContactLogic(btnAddContact, btnHome);
        initList();
    }

    //ADD CONTACT LOGIC
    public void addContactLogic(Button btnAddContact, pl.droidsonroids.gif.GifImageView btnHome) {
        //[Button Logic] Potato Corner
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContactDialog.setContentView(R.layout.popup_add_contacts);
                addContactDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                addContactDialog.show();
                addContactDialog.setCancelable(false);  //back press
                addContactDialog.setCanceledOnTouchOutside(false); //outside

                EditText etName = (EditText) addContactDialog.findViewById(R.id.et_addName);;
                EditText etNumber = (EditText) addContactDialog.findViewById(R.id.et_addNumber);
                EditText etEmail = (EditText) addContactDialog.findViewById(R.id.et_addEmail);
                Button btnAddContact = (Button) addContactDialog.findViewById(R.id.btn_createContact);
                pl.droidsonroids.gif.GifImageView btnReturn = (pl.droidsonroids.gif.GifImageView) addContactDialog.findViewById(R.id.btn_return);

                ivPhoto = findViewById(R.id.iv_avatar);
                btnUpload = findViewById(R.id.btn_upload);
//                btnCamera = findViewById(R.id.btn_camera);
//                btnGallery = findViewById(R.id.btn_gallery);
                storageRef = FirebaseStorage.getInstance().getReference();

                btnReturn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                        finish();
                    }
                });

                //button add contact logic
                btnAddContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        name = etName.getText().toString();
                        email = etEmail.getText().toString();
                        number = etNumber.getText().toString();

                        //A. Empty Validation
                        if (name.equals(""))
                            etName.setError("Name is required.");
                        if (email.equals(""))
                            etEmail.setError("Email is required.");
                        if (number.equals(""))
                            etNumber.setError("Number is required.");

                        //B. Validate Name
                        if (name.length() > 50)
                            etName.setError("Name should not exceed 50 characters.");
                        if (name.length() < 3)
                            etName.setError("Name should not be less than 3 characters.");
                        if (!name.matches("^([^0-9]*)$"))
                            etName.setError("Name should not contain numbers.");

                        //C. Validate Number
                        if (!number.matches("^[0-9]{11}$"))
                            etNumber.setError("Number should be 11 numerical digits.");

                        //D. Validate Email
                        if (!email.endsWith("@gmail.com"))
                            etEmail.setError("Please make sure your domain email is from @gmail.com");

                        /**************************
                         * E. write to file
                         *------------------------*/
                        if(email.endsWith("@gmail.com") && number.matches("^[0-9]{11}$") && name.matches("^([^0-9]*)$")) {

                            if (listModels.size() > 4) {
                                //show popup
                                Toast.makeText(getApplicationContext(),
                                        "Failed to add contact. Limited to 5 emergency contacts only. Please delete an existing one.",
                                        Toast.LENGTH_LONG).show();
                            }
                            else {
                                //write to file
                                ContactsModel.writeToFile(name, email, number, getApplicationContext());
                                //show popup
                                Toast.makeText(getApplicationContext(), "Successfully added new contact.", Toast.LENGTH_SHORT).show();
                            }

                            //close dialog
                            addContactDialog.dismiss();
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                            overridePendingTransition(0, 0);
                        }
                    }
                });


            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContactsActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void initList() {
        //read from file
        ContactsModel.readFromFile(listModels,getApplicationContext());
    }


    //EDIT CONTACT LOGIC
    @Override
    public void onItemClick(View childView, int position) {
        //call dialog
        editContactDialog.setContentView(R.layout.popup_edit_contacts);
        editContactDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editContactDialog.show();
        editContactDialog.setCancelable(false); //back press
        editContactDialog.setCanceledOnTouchOutside(false); //outside

        //declare and instantiate variables
        EditText et_name = (EditText) editContactDialog.findViewById(R.id.et_detailName);
        EditText et_number = (EditText) editContactDialog.findViewById(R.id.et_detailNumber);
        EditText et_email = (EditText) editContactDialog.findViewById(R.id.et_detailEmail);
        Button btn_edit = (Button) editContactDialog.findViewById(R.id.btn_editContact);
        pl.droidsonroids.gif.GifImageView btn_delete = (pl.droidsonroids.gif.GifImageView) editContactDialog.findViewById(R.id.btn_delete);
        pl.droidsonroids.gif.GifImageView btnReturn = (pl.droidsonroids.gif.GifImageView) editContactDialog.findViewById(R.id.btn_return);

        //set texts
        et_name.setText(listModels.get(position).getName());
        et_number.setText(listModels.get(position).getNumber());
        et_email.setText(listModels.get(position).getEmail());

        /****************************
         *  Button Click Listeners
         ****************************/
        //RETURN BUTTON
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                finish();
            }
        });
        //DELETE BUTTON
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactsModel.deleteFromFile(getApplicationContext(), position);
                startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                finish();
            }
        });
        //EDIT BUTTON
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = et_name.getText().toString();
                email = et_email.getText().toString();
                number = et_number.getText().toString();

                //A. Empty Validation
                if (name.equals(""))
                    et_name.setError("Name is required.");
                if (email.equals(""))
                    et_name.setError("Email is required.");
                if (number.equals(""))
                    et_number.setError("Number is required.");

                //B. Validate Name
                if (name.length() > 50)
                    et_name.setError("Name should not exceed 50 characters.");
                if (name.length() < 3)
                    et_name.setError("Name should not be less than 3 characters.");
                if (!name.matches("^([^0-9]*)$"))
                    et_name.setError("Name should not contain numbers.");

                //C. Validate Number
                if (!number.matches("^[0-9]{11}$"))
                    et_number.setError("Number should be 11 numerical digits.");

                //D. Validate Email
                if (!email.endsWith("@gmail.com"))
                    et_email.setError("Please make sure your domain email is from @gmail.com");

                /**************************
                 * E. edit to file
                 *------------------------*/
                if (email.endsWith("@gmail.com") && number.matches("^[0-9]{11}$") && name.matches("^([^0-9]*)$")) {

                    //show popup
                    Toast.makeText(getApplicationContext(), "Successfully edited contact. Returning to home for refresh.", Toast.LENGTH_SHORT).show();

                    //write to file
                    ContactsModel.editFile(name, email, number, getApplicationContext(), position);
                    startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                    finish();
                }
            }
        });
    }
}