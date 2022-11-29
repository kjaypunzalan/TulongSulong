package com.iacademy.tulongsulong.models;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iacademy.tulongsulong.activities.ContactsActivity;
import com.iacademy.tulongsulong.adapters.ContactsAdapter;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iacademy.tulongsulong.utils.RecyclerOnItemClickListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class ContactsModel {

    //declare variables
    private String name;
    private String email;
    private String number;
    private String imageURL;
    private String key;


    //constructor
    public ContactsModel(){};
    public ContactsModel(String name, String email, String number) {
        this.name = name;
        this.email = email;
        this.number = number;
    }
    public ContactsModel(String name, String email, String number, String key, String imageURL) {
        this.name = name;
        this.email = email;
        this.number = number;
        this.key = key;
        this.imageURL = imageURL;
    }

    //setters and getters
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    public void setNumber(String number) { this.number = number; }
    public String getNumber() { return number; }

    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public String getImageURL() { return imageURL; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }


    /**************************
     * A. read from firebase
     *------------------------*/
    public static void readFromFile(
            ArrayList<ContactsModel> listModels,
            ArrayList<String> keys,
            RecyclerView rvList,
            Context context,
            DatabaseReference mReference,
            FirebaseAuth mAuth,
            RecyclerOnItemClickListener item) {


        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot data : snapshot.getChildren()){
                    listModels.add(data.getValue(ContactsModel.class));
                    keys.add(data.getKey());
                }

                ContactsAdapter contactsAdapter = new ContactsAdapter(listModels, context);
                contactsAdapter.notifyDataSetChanged();
                rvList.setAdapter(new ContactsAdapter(listModels, context, item));

//                for(DataSnapshot data : snapshot.getChildren()){
//                    listModels.add(new ContactsModel(
//                            data.child("name").getValue().toString(),
//                            data.child("email").getValue().toString(),
//                            data.child("number").getValue().toString()));
//                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "FAILURE TO READ DATABASE", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**************************
     * B. write to firebase
     *------------------------*/
    public static void writeToFile(
            String name,
            String email,
            String number,
            Context context,
            DatabaseReference mReference,
            FirebaseAuth mAuth,
            StorageReference storageRef,
            ImageView ivAvatar) {

        /*********************
         * NOTES
         * DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
         * StorageReference storageRef = FirebaseStorage.getInstance().getReference();
         *
         */
        //FIREBASE INSTANTIATION
        FirebaseDatabase firebaseDB = FirebaseDatabase.getInstance();       //firebase instance
        final DatabaseReference reference = firebaseDB.getReference();      //firebase reference

        //REFERENCE USER AND CONTACT
        DatabaseReference referenceUser = reference.child("Users").child(mAuth.getCurrentUser().getUid());  //user uid
        DatabaseReference referenceContact = referenceUser.push();                                          //push contact key

        /*********************
         * AVATAR!!!!!!!
         */
        try {
            //long unixTime = System.currentTimeMillis() / 1000L;
            StorageReference imageRef = storageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("image - " + number + ".jpg");

            Bitmap bitmap = ((BitmapDrawable) ivAvatar.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //PUT TO REALTIME DATABASE
                            String imageReference = uri.toString();
                            DatabaseReference referenceImage = referenceUser.child(referenceContact.getKey()).child("imageURL");
                            referenceImage.setValue(imageReference);

                            //POPULATE DATA
                            ContactsModel contactInfo = new ContactsModel(name, email, number, referenceContact.getKey(), imageReference);
                            referenceContact.setValue(contactInfo);
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(context, "Default Image Added", Toast.LENGTH_SHORT).show();
            //PUT TO REALTIME DATABASE
            String imageReference = "https://iili.io/HKrlhCP.gif";
            DatabaseReference referenceImage = referenceUser.child(referenceContact.getKey()).child("imageURL");
            referenceImage.setValue(imageReference);

            //POPULATE DATA
            ContactsModel contactInfo = new ContactsModel(name, email, number, referenceContact.getKey(), imageReference);
            referenceContact.setValue(contactInfo);
        }
    }

    /**************************
     * C. edit to firebase
     *------------------------*/
    public static void editFile(String name,
                                String email,
                                String number,
                                Context context,
                                DatabaseReference mReference,
                                FirebaseAuth mAuth,
                                StorageReference storageRef,
                                ImageView ivAvatar,
                                int position,
                                ArrayList<ContactsModel> listModels) {

        HashMap hashMap = new HashMap();
        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("number", number);

        /*********************
         * NOTES
         * DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
         * StorageReference storageRef = FirebaseStorage.getInstance().getReference();
         *
         */
        //FIREBASE INSTANTIATION
        FirebaseDatabase firebaseDB = FirebaseDatabase.getInstance();       //firebase instance
        final DatabaseReference reference = firebaseDB.getReference();      //firebase reference

        //REFERENCE USER AND CONTACT
        DatabaseReference referenceUser = reference.child("Users").child(mAuth.getCurrentUser().getUid());  //user uid
        DatabaseReference referenceContact = referenceUser.child(listModels.get(position).getKey());                                          //push contact key

        /*********************
         * AVATAR!!!!!!!
         */
        try {
            //long unixTime = System.currentTimeMillis() / 1000L;
            storageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("image - " + number + ".jpg").delete();
            StorageReference imageRef = storageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("image - " + number + ".jpg");

            Bitmap bitmap = ((BitmapDrawable) ivAvatar.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(context, imageRef.getPath(), Toast.LENGTH_SHORT).show();
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //PUT TO REALTIME DATABASE
                            String imageReference = uri.toString();
                            hashMap.put("imageURL", imageReference);

                            //UPDATE DATA
                            referenceContact.updateChildren(hashMap);
                        }
                    });
                }
            });
        } catch (Exception e) {
            //UPDATE DATA
            referenceContact.updateChildren(hashMap);
        }
    }

    /**************************
     * D. delete from firebase
     *------------------------*/
    public static void deleteFromFile(String number,
                                      Context context,
                                      DatabaseReference mReference,
                                      FirebaseAuth mAuth,
                                      StorageReference storageRef,
                                      ImageView ivAvatar,
                                      int position,
                                      ArrayList<ContactsModel> listModels) {

        storageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("image - " + number + ".jpg").delete();
        mAuth = FirebaseAuth.getInstance();
        mReference.child("Users").child(mAuth.getCurrentUser().getUid()).child(listModels.get(position).getKey()).removeValue();
    }

    /**************************
     * E. upload image
     *------------------------*/
    public static void uploadImage(Context context,
                                   pl.droidsonroids.gif.GifImageView btnCamera,
                                   pl.droidsonroids.gif.GifImageView btnGallery,
                                   ActivityResultLauncher<Intent> cameraIntentLauncher,
                                   ActivityResultLauncher<Intent> galleryIntentLauncher,
                                   Activity activity) {

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*******************
                 * REQUEST PERMISSION
                 *******************/
                String[] PERMISSIONS = {Manifest.permission.CAMERA};
                if(!hasPermissions(context, PERMISSIONS)){
                    ActivityCompat.requestPermissions(activity, PERMISSIONS, 1);
                }else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntentLauncher.launch(intent);
                }
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                galleryIntentLauncher.launch(chooserIntent);
            }
        });
    }

    /**************************
     * F. image upload permission
     *------------------------*/
    private static boolean hasPermissions(Context context, String... permissions){
        for(String permission : permissions){
            if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }

        return true;
    }
}
