package com.iacademy.tulongsulong.activities;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iacademy.tulongsulong.models.ContactsModel;
import com.iacademy.tulongsulong.adapters.ContactsAdapter;
import com.iacademy.tulongsulong.utils.RecyclerOnItemClickListener;
import com.iacademy.tulongsulong.R;
import com.iacademy.tulongsulong.activities.LoadScreenActivity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity implements RecyclerOnItemClickListener {

    //DECLARE VARIABLES
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    private ContactsModel contactInfo;
    private RecyclerView rvContactList;
    private ArrayList<ContactsModel> listModels = new ArrayList<>();
    private ArrayList<String> keys = new ArrayList<>();

    private pl.droidsonroids.gif.GifImageView btnHome;
    private Button btnAddContact;
    private Dialog addContactDialog;
    private Dialog editContactDialog;
    private String name;
    private String email;
    private String number;

    //ADD CONTACT VARIABLES
    private EditText etName, etNumber, etEmail;
    private pl.droidsonroids.gif.GifImageView btnReturn;
    private int PICK_IMAGE = 100;

    //EDIT CONTACT VARIABLES
    private EditText et_name, et_number, et_email;
    private Button btn_edit, btnCreateContact;
    private pl.droidsonroids.gif.GifImageView btn_delete;

    //FIREBASE VARIABLES
    private FirebaseAuth mAuth;                     //authorization
    private DatabaseReference mReference;   //realtime database
    private StorageReference storageRef;    //storage
    //IMAGE UPLOAD VARIABLES
    private ImageView ivAvatar;
    private pl.droidsonroids.gif.GifImageView btnCamera, btnGallery;
    private ActivityResultLauncher<Intent> cameraIntentLauncher, galleryIntentLauncher;



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
        mReference = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        //Recycler View logic
        rvContactList.setLayoutManager(new LinearLayoutManager(ContactsActivity.this, LinearLayoutManager.VERTICAL, false));
        rvContactList.setAdapter(new ContactsAdapter(listModels, ContactsActivity.this, this)); //set adaptor that provide child views

        //call void methods
        cameraGallery(); //camera and gallery launchers
        addContactLogic(btnAddContact, btnHome, mReference);
        initList();
    }

    /***************************
     * A. INITIALIZE DATA
     *------------------------*/
    private void initList() {
        //read from file
        ContactsModel.readFromFile(listModels, keys, rvContactList, getApplicationContext(), mReference, mAuth, this);
    }

    /**********************************
     * B. CAMERA AND GALLERY LAUNCHERS
     *-------------------------------*/
    public void cameraGallery() {
        cameraIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            Bitmap photo = (Bitmap) data.getExtras().get("data");
                            ivAvatar.setImageBitmap(photo);
                        }
                    }
                });
        galleryIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            try {
                                Intent data = result.getData();
                                Uri imageUri = data.getData();
                                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                ivAvatar.setImageBitmap(selectedImage);

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    /***************************
     * C. ADD CONTACT LOGIC
     *------------------------*/
    public void addContactLogic(Button btnAddContact,
                                pl.droidsonroids.gif.GifImageView btnHome,
                                DatabaseReference mReference) {

        //BUTTON ADD CONTACT
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DIALOG
                addContactDialog.setContentView(R.layout.popup_add_contacts);
                addContactDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                addContactDialog.show();
                addContactDialog.setCancelable(false);  //back press
                addContactDialog.setCanceledOnTouchOutside(false); //outside

                //ADD CONTACT DIALOG VARIABLES
                etName = (EditText) addContactDialog.findViewById(R.id.et_addName);;
                etNumber = (EditText) addContactDialog.findViewById(R.id.et_addNumber);
                etEmail = (EditText) addContactDialog.findViewById(R.id.et_addEmail);
                btnCreateContact = (Button) addContactDialog.findViewById(R.id.btn_createContact);
                btnReturn = (pl.droidsonroids.gif.GifImageView) addContactDialog.findViewById(R.id.btn_return);

                //UPLOAD AVATAR CONTACT DIALOG VARIABLES
                ivAvatar = (ImageView) addContactDialog.findViewById(R.id.iv_editAvatar);
                btnCamera = (pl.droidsonroids.gif.GifImageView) addContactDialog.findViewById(R.id.btn_editCamera);
                btnGallery = (pl.droidsonroids.gif.GifImageView) addContactDialog.findViewById(R.id.btn_editGallery);
                //UPLOAD IMAGE
                ContactsModel.uploadImage(getApplicationContext(), btnCamera, btnGallery, cameraIntentLauncher, galleryIntentLauncher, ContactsActivity.this);


                //BUTTON RETURN TO HOME
                btnReturn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addContactDialog.dismiss();
                        startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                        finish();
                    }
                });

                //BUTTON CREATE CONTACT LOGIC
                btnCreateContact.setOnClickListener(new View.OnClickListener() {
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
                                //WRITE CONTACT TO FILE
                                ContactsModel.writeToFile(name, email, number, getApplicationContext(), mReference, mAuth, storageRef, ivAvatar);
                                //show popup
                                Toast.makeText(getApplicationContext(), "Successfully added new contact. Please wait for refresh.", Toast.LENGTH_SHORT).show();
                            }

                            //close dialog
                            addContactDialog.dismiss();
                            finish();
                            startActivity(new Intent(ContactsActivity.this, LoadScreenActivity.class));
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


    /**********************************
     * D. EDIT CONTACT LOGIC
     *-------------------------------*/
    @Override
    public void onItemClick(View childView, int position) {

        //call dialog
        editContactDialog.setContentView(R.layout.popup_edit_contacts);
        editContactDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editContactDialog.show();
        editContactDialog.setCancelable(false);  //back press
        editContactDialog.setCanceledOnTouchOutside(false); //outside

        //declare and instantiate variables
        et_name = (EditText) editContactDialog.findViewById(R.id.et_detailName);
        et_number = (EditText) editContactDialog.findViewById(R.id.et_detailNumber);
        et_email = (EditText) editContactDialog.findViewById(R.id.et_detailEmail);
        btn_edit = (Button) editContactDialog.findViewById(R.id.btn_editContact);
        btn_delete = (pl.droidsonroids.gif.GifImageView) editContactDialog.findViewById(R.id.btn_delete);
        btnReturn = (pl.droidsonroids.gif.GifImageView) editContactDialog.findViewById(R.id.btn_return);

        //UPLOAD AVATAR CONTACT DIALOG VARIABLES
        ivAvatar = (ImageView) editContactDialog.findViewById(R.id.iv_editAvatar);
        btnCamera = (pl.droidsonroids.gif.GifImageView) editContactDialog.findViewById(R.id.btn_editCamera);
        btnGallery = (pl.droidsonroids.gif.GifImageView) editContactDialog.findViewById(R.id.btn_editGallery);
        //UPLOAD IMAGE
        ContactsModel.uploadImage(getApplicationContext(), btnCamera, btnGallery, cameraIntentLauncher, galleryIntentLauncher, ContactsActivity.this);

        //set texts
        Picasso.get().load(listModels.get(position).getImageURL()).into(ivAvatar);
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
                editContactDialog.dismiss();
                startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                finish();
            }
        });
        //DELETE BUTTON
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = et_number.getText().toString();
                ContactsModel.deleteFromFile(number, getApplicationContext(), mReference, mAuth, storageRef, ivAvatar, position, listModels);
                Toast.makeText(getApplicationContext(), "Successfully deleted contact. Please wait for refresh.", Toast.LENGTH_SHORT).show();
                editContactDialog.dismiss();
                startActivity(new Intent(ContactsActivity.this, LoadScreenActivity.class));
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
                    Toast.makeText(getApplicationContext(), "Successfully edited contact. Please wait for refresh.", Toast.LENGTH_SHORT).show();

                    //write to file
                    ContactsModel.editFile(name, email, number, getApplicationContext(), mReference, mAuth, storageRef, ivAvatar, position, listModels);
                    editContactDialog.dismiss();
                    startActivity(new Intent(ContactsActivity.this, LoadScreenActivity.class));
                    finish();
                }
            }
        });
    }
}