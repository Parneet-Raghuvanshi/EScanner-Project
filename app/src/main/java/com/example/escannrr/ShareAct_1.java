package com.example.escannrr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class ShareAct_1 extends AppCompatActivity {

    LinearLayout layout_pdf,layout_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_act_1);

        layout_pdf = findViewById(R.id.layout_pdf);
        layout_image = findViewById(R.id.layout_image);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*1),(int)(height*.32));
        getWindow().setGravity(Gravity.BOTTOM);

        layout_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SHAREACT "," SHARED PDF------------------------------------------------");
                ArrayList<Uri> filesUris = new ArrayList<>();
                for (int i=0;i<((MyApplication) getApplication()).getSharepdf().size();i++){
                    filesUris.add(Uri.fromFile(new File(((MyApplication) getApplication()).getSharepdf().get(i))));
                }
                final Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("application/pdf");
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesUris);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_snackbar)));
                finish();
            }
        });
        layout_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SHAREACT "," SHARED PHOTOS------------------------------------------------");
                ArrayList<Uri> filesUris = new ArrayList<>();
                for (int i=0;i<((MyApplication) getApplication()).getShareimages().size();i++){
                    filesUris.add(getImageUri(ShareAct_1.this,((MyApplication)getApplication()).getShareimages().get(i)));
                }
                final Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("image/jpg");
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesUris);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_snackbar)));
                finish();
            }
        });
    }

    public static Uri getImageUri(Activity inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}