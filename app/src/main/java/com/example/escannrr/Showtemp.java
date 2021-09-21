package com.example.escannrr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class Showtemp extends AppCompatActivity {

    ImageView imageView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtemp);

        imageView = findViewById(R.id.imageview);

        imageView.setImageBitmap(((MyApplication) this.getApplication()).getBitmap());
    }
}