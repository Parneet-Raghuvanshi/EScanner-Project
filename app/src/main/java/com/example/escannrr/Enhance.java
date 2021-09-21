package com.example.escannrr;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import kotlin.jvm.internal.Intrinsics;

public class Enhance extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;
    String docname;
    Toolbar toolbar;
    int choice = 2;
    Bitmap b,magic_b;
    PhotoView imageView;
    LinearLayout backbtn,nextbtn,ocrbtn;
    ImageView filter_1,filter_2,filter_3,filter_4,filter_5,filter_6;
    LinearLayout filter_1l,filter_2l,filter_3l,filter_4l,filter_5l,filter_6l;
    TextView vechile_number;

    @Override
    protected void onResume() {
        docname = ((MyApplication) this.getApplication()).getDocname();
        toolbar.setTitle(docname);
        super.onResume();
    }

    @Override
    protected void onStart() {
        docname = ((MyApplication) this.getApplication()).getDocname();
        toolbar.setTitle(docname);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_enhance);

        vechile_number = findViewById(R.id.vechile_number);
        ocrbtn = findViewById(R.id.ocrbtn);

        ocrbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocr();
            }
        });

        docname = ((MyApplication) this.getApplication()).getDocname();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(docname);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Enhance.this,Renameact.class);
                startActivity(intent);
            }
        });

        b = ((MyApplication) this.getApplication()).getBitmap();

        imageView = findViewById(R.id.imagebtmp);
        backbtn = findViewById(R.id.backbtn);
        nextbtn = findViewById(R.id.nextbtn);

        magic_b = enhanceImg(b);
        imageView.setImageBitmap(magic_b);

        setUpScrolls();

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Enhance.this);
                builder.setTitle("Note");
                builder.setMessage("Are you sure you want to discard this image?")
                        .setCancelable(false)
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                docname = ((MyApplication) getApplication()).getDocname();
                ((MyApplication) getApplication()).setSavedstate(true);
                ArrayList<Bitmap> bitmaps = new ArrayList<>();
                if (((MyApplication) getApplication()).getDetail()!=null){
                    bitmaps.addAll(((MyApplication)getApplication()).getDetail());
                }
                bitmaps.add(((MyApplication) getApplication()).getBitmap());
                ((MyApplication) getApplication()).setDetail(bitmaps);
                String pdfFile = Environment.getExternalStorageDirectory().toString() + "/Escanrr/"+docname+ ".pdf";
                ((MyApplication) getApplication()).setPath(pdfFile);
                Intent intent = new Intent(Enhance.this,SaveAct.class);
                startActivity(intent);
            }
        });
    }

    private void ocr() {
        Bitmap bitmap = ((MyApplication) this.getApplication()).getBitmap();

        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!recognizer.isOperational()) {
            Toast.makeText(this, "Error , Please try Again", Toast.LENGTH_SHORT).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = recognizer.detect(frame);
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < items.size(); i++) {
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                sb.append("\n");
            }

            //String[] lines = sb.toString().split("\r\n|\r|\n");

            SaveDoc(sb.toString());
            vechile_number.setText(sb.toString());
        }
    }

    private void SaveDoc(String toString) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
        }
        else {
            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(toString);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),"Data.docx");
            OutputStream outputStream = null;

            try {
                outputStream=new FileOutputStream(file);;
                doc.write(outputStream);
                Toast.makeText(getApplicationContext(),"Saved in Downloads",Toast.LENGTH_LONG).show();
            } catch (java.io.IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error Occured",Toast.LENGTH_LONG).show();
                try {
                    if (outputStream!=null)
                        outputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private Bitmap enhanceImg(Bitmap b) {
        Mat src_mat = new Mat();
        Utils.bitmapToMat(b, src_mat);
        Imgproc.cvtColor(src_mat, src_mat, 11);
        Imgproc.adaptiveThreshold(src_mat, src_mat, 255.0D, 0, 0, 15, 15.0D);
        Bitmap result = Bitmap.createBitmap(b != null ? b.getWidth() : 1080, b != null ? b.getHeight() : 1920, Bitmap.Config.RGB_565);
        Utils.matToBitmap(src_mat, result, true);
        src_mat.release();
        Intrinsics.checkExpressionValueIsNotNull(result, "result");
        return result;
    }

    private void setUpScrolls() {
        filter_1 = findViewById(R.id.filter_1);
        filter_2 = findViewById(R.id.filter_2);
        filter_3 = findViewById(R.id.filter_3);
        filter_4 = findViewById(R.id.filter_4);
        filter_5 = findViewById(R.id.filter_5);
        filter_6 = findViewById(R.id.filter_6);

        filter_1.setImageBitmap(b);
        filter_2.setImageBitmap(magic_b);
        filter_3.setImageBitmap(b);
        filter_4.setImageBitmap(b);
        filter_5.setImageBitmap(b);
        filter_6.setImageBitmap(b);

        filter_1l = findViewById(R.id.filter_1_layout);
        filter_2l = findViewById(R.id.filter_2_layout);
        filter_3l = findViewById(R.id.filter_3_layout);
        filter_4l = findViewById(R.id.filter_4_layout);
        filter_5l = findViewById(R.id.filter_5_layout);
        filter_6l = findViewById(R.id.filter_6_layout);

        filter_1l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choice!=1){
                    filter_1l.setBackground(getResources().getDrawable(R.drawable.filter_selected));
                    filter_2l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_3l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_4l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_5l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_6l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));

                    imageView.setImageBitmap(b);
                    choice = 1;
                }
            }
        });

        filter_2l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choice!=2){
                    filter_1l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_2l.setBackground(getResources().getDrawable(R.drawable.filter_selected));
                    filter_3l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_4l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_5l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_6l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));

                    imageView.setImageBitmap(magic_b);
                    choice = 2;
                }
            }
        });

        filter_3l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choice!=3){
                    filter_1l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_2l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_3l.setBackground(getResources().getDrawable(R.drawable.filter_selected));
                    filter_4l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_5l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_6l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    choice = 3;
                }
            }
        });

        filter_4l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choice!=4){
                    filter_1l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_2l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_3l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_4l.setBackground(getResources().getDrawable(R.drawable.filter_selected));
                    filter_5l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_6l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    choice = 4;
                }
            }
        });

        filter_5l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choice!=5){
                    filter_1l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_2l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_3l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_4l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_5l.setBackground(getResources().getDrawable(R.drawable.filter_selected));
                    filter_6l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    choice = 5;
                }
            }
        });

        filter_6l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choice!=6){
                    filter_1l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_2l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_3l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_4l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_5l.setBackground(getResources().getDrawable(R.drawable.filter_unselected));
                    filter_6l.setBackground(getResources().getDrawable(R.drawable.filter_selected));
                    choice = 6;
                }
            }
        });
    }
}
