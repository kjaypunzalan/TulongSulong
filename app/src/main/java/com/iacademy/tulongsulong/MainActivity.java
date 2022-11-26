package com.iacademy.tulongsulong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    int PERMISSIONS_REQUEST_SMS_SEND = 1;
    Button btnLogout, btnSos;
    ImageView ivLogoutIcon;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hide action bar
        getSupportActionBar().hide();

        btnLogout = findViewById(R.id.btnLogout);
        //bring logout icon to front

        btnSos = findViewById(R.id.btnSos);
        ivLogoutIcon = findViewById(R.id.iv_logouticon);
        ivLogoutIcon.setZ(100);

        mAuth = FirebaseAuth.getInstance();

        btnSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED){
                    sendMessage();
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest
                            .permission.SEND_SMS}, PERMISSIONS_REQUEST_SMS_SEND);

                    }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {super
            .onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSIONS_REQUEST_SMS_SEND && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            sendMessage();
        }else{
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendMessage(){
        String message = "I am in DANGER, please send help immediately!";
//        String number = "09296143204"; for testing

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, null, null); //getNumber from contact list
        Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();

    }
}