package com.example.escannrr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MoreAct extends AppCompatActivity {

    TextView docname;
    LinearLayout rename_btn,select_btn,ocr_btn,email_btn,sort_btn,invite_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        docname = findViewById(R.id.window_title);
        rename_btn = findViewById(R.id.rename_btn);
        select_btn = findViewById(R.id.select_btn);
        ocr_btn = findViewById(R.id.ocr_btn);
        email_btn = findViewById(R.id.email_btn);
        sort_btn = findViewById(R.id.sort_btn);
        invite_btn = findViewById(R.id.invite_btn);

        docname.setText(((MyApplication)getApplicationContext()).getDocname());

        rename_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreAct.this,Renameact.class);
                startActivity(intent);
                finish();
            }
        });
        select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ocr_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        email_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        sort_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        invite_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}