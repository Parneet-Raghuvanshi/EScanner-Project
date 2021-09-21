package com.example.escannrr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ColumnHead extends AppCompatActivity {

    EditText text_docname;
    TextView doc_name_settings,ok_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_column_head);

        text_docname = findViewById(R.id.text_docname);
        doc_name_settings = findViewById(R.id.doc_name_settings);
        ok_btn = findViewById(R.id.ok_btn);

        text_docname.setHint("Column Title Here");

        text_docname.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (text_docname.getRight() - text_docname.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        text_docname.setText(null);
                        return true;
                    }
                }
                return false;
            }
        });

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text_docname.getText()!=null){
                    ((MyApplication) getApplication()).setClmname(text_docname.getText().toString());
                    Intent intent = new Intent();
                    setResult(2255,intent);
                    finish();
                }
                else {
                    Toast.makeText(ColumnHead.this, "Please enter Column title", Toast.LENGTH_SHORT).show();
                }
            }
        });

        doc_name_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}