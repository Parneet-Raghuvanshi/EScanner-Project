package com.example.escannrr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Renameact extends AppCompatActivity {

    EditText text_docname;
    TextView doc_name_settings,ok_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renameact);

        text_docname = findViewById(R.id.text_docname);
        doc_name_settings = findViewById(R.id.doc_name_settings);
        ok_btn = findViewById(R.id.ok_btn);

        text_docname.setText(((MyApplication) this.getApplication()).getDocname());
        text_docname.setSelectAllOnFocus(true);

        text_docname.setHint(((MyApplication) this.getApplication()).getDocname());

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

        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*1),(int)(height*.8));
        getWindow().setGravity(Gravity.BOTTOM);*/

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text_docname.getText()!=null){
                    ((MyApplication) getApplication()).setDocname(text_docname.getText().toString());
                    finish();
                }
                else {
                    Toast.makeText(Renameact.this, "Please enter document name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        doc_name_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Renameact.this, "Doc Settings", Toast.LENGTH_SHORT).show();
            }
        });
    }
}