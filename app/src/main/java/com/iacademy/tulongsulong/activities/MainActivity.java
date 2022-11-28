package com.iacademy.tulongsulong.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iacademy.tulongsulong.R;
import com.iacademy.tulongsulong.models.ContactsModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /*******************
     * DECLARE VARIABLES
     *******************/
    //primary variables
    private static final int PERMISSIONS_REQUEST_SMS_SEND = 1;
    private Button btnLogout;
    private pl.droidsonroids.gif.GifImageView btnSOS, btnContactList;
    private ArrayList<ContactsModel> listModels = new ArrayList<>();
    private ImageView ivLogoutIcon;

    //gps variables
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private static final long DEFAULT_UPDATE_INTERVAL = 5000;
    private TextView tvLatitude, tvLongitude, tvAddress;
    private String realAddress;
    private LocationRequest locationRequest; //config file for all settings related to FusedLocationProviderClient
    private FusedLocationProviderClient fusedLocationProviderClient; //Google's API for location services

    //firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hide action bar
        getSupportActionBar().hide();

        //instantiate variables
        ivLogoutIcon = findViewById(R.id.iv_logouticon);
        btnLogout = findViewById(R.id.btnLogout);
        btnSOS = findViewById(R.id.btnSOS);
        btnContactList = findViewById(R.id.btnContactList);
        tvLatitude = findViewById(R.id.tv_lat);
        tvLongitude = findViewById(R.id.tv_lon);
        tvAddress = findViewById(R.id.tv_address);

        //instantiate API
        mAuth = FirebaseAuth.getInstance();
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, DEFAULT_UPDATE_INTERVAL).build();

        //bring logout icon to front
        bringToFront(ivLogoutIcon);

        btnContactList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ContactsActivity.class));
                finish();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        updateGPS(btnSOS);
    }

    /*******************
     * FIREBASE
     *******************/
    protected void bringToFront(ImageView icon1) {
        icon1.setZ(100);
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    /*******************
     * REQUEST PERMISSION
     *******************/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {super
            .onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS(btnSOS);
                }
                break;
            case PERMISSIONS_REQUEST_SMS_SEND:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0) {
                    sendMessage(realAddress);
                }
                break;
            default:
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show(); finish();
                break;
        }
    }

    /*******************
     * GPS
     *******************/
    private void updateGPS(pl.droidsonroids.gif.GifImageView btnSOS) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        //user provided permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //put values into UI
                    tvLatitude.setText(String.valueOf(location.getLatitude()));
                    tvLongitude.setText(String.valueOf(location.getLongitude()));

                    //real address time
                    Geocoder geocoder = new Geocoder(MainActivity.this);

                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                1);
                        tvAddress.setText(addresses.get(0).getAddressLine(0));
                        realAddress = String.valueOf(addresses.get(0).getAddressLine(0));

                        btnSOS.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                                        == PackageManager.PERMISSION_GRANTED){
                                    sendMessage(realAddress);
                                }else{
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest
                                            .permission.SEND_SMS}, PERMISSIONS_REQUEST_SMS_SEND);

                                }
                            }
                        });
                    } catch(Exception e) {
                        tvAddress.setText("Unable to get exact address.");
                    }
                }
            });
        }
        //else, request permission
        else {
            //permission not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    /*******************
     * SEND SOS
     *******************/
    public void sendMessage(String realAddress){
//        String message = "I am in DANGER, please send help immediately! I am currently in " + realAddress;
//
//        String number = "09171103109";
//
//        SmsManager sms = SmsManager.getDefault();
//        sms.sendTextMessage(number, null, message, null, null); //getNumber from contact list
//        Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                SmsManager sms = SmsManager.getDefault();
                for (DataSnapshot data : snapshot.getChildren()) {
                    //get value
                    String name = data.child("name").getValue().toString();
                    String number = data.child("number").getValue().toString();

                    //message the nsend
                    String message = "HELP ME, " + name + "!! I am in DANGER, please send help immediately! I am currently in " + realAddress;

                    sms.sendTextMessage(number, null, message, null, null); //getNumber from contact list
                }
                Toast.makeText(getApplicationContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show();
            }
        });
    }
}