package com.iacademy.tulongsulong.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.iacademy.tulongsulong.R;

public class LoadScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_screen);

        //hide action bar
        getSupportActionBar().hide();

        //after a delay, go to Login Activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LoadScreenActivity.this, ContactsActivity.class));
                finish();
            }
        }, 5000);
    }
}