package com.iacademy.tulongsulong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.iacademy.tulongsulong.R;

public class LoginActivity extends AppCompatActivity {

    //declare variables
    private TextView tvRegister;
    private EditText etLogEmail, etLogPassword;
    private Button btnLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //hide action bar
        getSupportActionBar().hide();

        //instantiate variables
        etLogEmail = findViewById(R.id.etLogEmail);
        etLogPassword = findViewById(R.id.etLogPassword);
        tvRegister = findViewById(R.id.tvSignup);
        btnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser(){
        String email = etLogEmail.getText().toString();
        String password = etLogPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            etLogEmail.setError("Email cannot be empty");
            etLogEmail.requestFocus();
        }else if(TextUtils.isEmpty(password)){
            etLogPassword.setError("Password cannot be empty");
            etLogPassword.requestFocus();
        }else{
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener
                    (new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "User logged in successfully",
                                        Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }else{
                                Toast.makeText(LoginActivity.this, "Login Error: " + task
                                        .getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}