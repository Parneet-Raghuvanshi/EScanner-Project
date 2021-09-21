package com.example.escannrr;///////  274 pg

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Writexls extends AppCompatActivity {

    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int GET_CAMERA_CODE = 1001;
    Button previewbtn,addnewbtn,addrootbtn;
    ImageView image_root;
    String cameraPermission[];
    Uri image_uri;
    Uri resultUri;
    ImageView tempimg;
    boolean rootaval = false;
    ArrayList<ArrayList<String>> maindata = new ArrayList<>();
    Row[] rows;
    int rowno;
    Button temp_btn;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writexls);

        previewbtn = findViewById(R.id.btn_preview);
        addnewbtn = findViewById(R.id.add_new_clm);
        addnewbtn.setVisibility(View.GONE);
        addrootbtn = findViewById(R.id.add_root_btn);
        image_root = findViewById(R.id.image_root);
        tempimg = findViewById(R.id.temp_image);
        temp_btn = findViewById(R.id.btn_temp);
        textView = findViewById(R.id.test_temp);

        temp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(Writexls.this.getContentResolver() , resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if (!recognizer.isOperational()) {
                    Toast.makeText(Writexls.this, "Error , Please try Again", Toast.LENGTH_SHORT).show();
                } else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < items.size(); i++) {
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }

                    Log.d("MAIN STRING  ","  VALUE ==  "+sb.toString());
                    textView.setText(sb.toString());
                    String[] lines = sb.toString().split("\r\n|\r|\n");
                    for (int i=0;i<lines.length;i++){
                        Log.d("LINE  =  "+i,"    Value  ===  "+lines[i]);
                    }
                }
            }
        });

        addrootbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();
            }
        });

        addnewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Writexls.this,ColumnHead.class);
                startActivityForResult(intent,2255);
            }
        });

        previewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createXls();
            }
        });
    }

    private void createXls() {
        Workbook wb=new HSSFWorkbook();
        Cell cell=null;
        CellStyle cellStyle=wb.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.BLUE.index);
        Sheet sheet=null;
        sheet = wb.createSheet("Name of sheet");

        rows = new Row[rowno+1];

        for (int i=0;i<rowno+1;i++){
            rows[i] = sheet.createRow(i);
        }

        for (int j=0;j<maindata.size();j++){
            ArrayList<String> temp = new ArrayList<>();
            temp = maindata.get(j);
            int maxno = temp.get(0).length();
            for (int i=0;i<rowno+1;i++){
                cell = rows[i].createCell(j);
                cell.setCellValue(temp.get(i));
                cell.setCellStyle(cellStyle);
                if (maxno<temp.get(i).length()){
                    maxno = temp.get(i).length();
                }
            }
            int neww = (maxno*25)+20;
            sheet.setColumnWidth(j,(10*neww));
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),"test.xls");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            wb.write(outputStream);
            Toast.makeText(getApplicationContext(),"OK",Toast.LENGTH_LONG).show();
        } catch (java.io.IOException e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"NO OK",Toast.LENGTH_LONG).show();
            try {
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showOptions() {
        if (!checkCP()){
            requestCP();
        }
        else {
            startCamera();
        }
    }

    private void startCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Number Plate");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Choose Your Vechile Plate");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,GET_CAMERA_CODE);
    }

    private void requestCP() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCP() {
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result1 && result2;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        startCamera();
                    }
                    else {
                        Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2255){
            CropImage.activity(resultUri).start(Writexls.this);
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == GET_CAMERA_CODE) {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(Writexls.this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (rootaval){
                    Uri result_uri = result.getUri();

                    tempimg.setImageURI(result_uri);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) tempimg.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();

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

                        String[] lines = sb.toString().split("\r\n|\r|\n");
                        ArrayList<String> newclm = new ArrayList<>();
                        newclm.add(((MyApplication) getApplication()).getClmname());
                        for (int i = 0 ; i<lines.length;i++){
                            newclm.add(lines[i]);
                        }
                        maindata.add(newclm);
                        rowno = lines.length;
                    }
                }

                else {
                    Uri result_uri = result.getUri();
                    resultUri = result.getUri();

                    image_root.setImageURI(result_uri);
                    rootaval = true;
                    addnewbtn.setVisibility(View.VISIBLE);
                    addrootbtn.setVisibility(View.GONE);
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception exception = result.getError();
                Toast.makeText(this, "" + exception, Toast.LENGTH_SHORT).show();
            }
        }
    }
}