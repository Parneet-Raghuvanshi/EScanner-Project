package com.example.escannrr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            Intent homeintent = new Intent(SplashActivity.this, DashBoard.class);
            startActivity(homeintent);
            finish();
        }
        else {
            Intent homeintent = new Intent(SplashActivity.this, Register.class);
            startActivity(homeintent);
            finish();
        }
    }
}