package com.example.escannrr;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escannrr.uipr.scan.ScanActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.squareup.picasso.Picasso;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashBoard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    public Context context;
    private FloatingActionButton floatingActionButton;
    private CircleImageView userimg;
    TextView username;
    private RecyclerView recyclerView;
    ImageView imageView;
    private ArrayList<ArrayList<String>> pdfdata = new ArrayList<ArrayList<String>>();
    private ArrayList<Bitmap> preview = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dash_board);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv_all_docs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Escanrr");

        searchPdfInExternalStorage(folder);

        ((MyApplication) getApplication()).setPreview(preview);

        recyclerView.setAdapter(new AlldocAdapter(pdfdata,this));

        floatingActionButton = findViewById(R.id.floating_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
                Date date = new Date();
                String documentname = "New Doc "+formatter.format(date);
                ((MyApplication) getApplication()).setDocname(documentname);
                Intent intent = new Intent(DashBoard.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View view = navigationView.getHeaderView(0);
        username = view.findViewById(R.id.user_name_navhead);
        userimg = view.findViewById(R.id.user_img_navhead);

        userimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User_md user_md = dataSnapshot.getValue(User_md.class);

                username.setText(user_md.getName());

                if (user_md.getImguri() != null){
                    Picasso.get().load(user_md.getImguri()).into(userimg);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_dashboard,new Alldocs()).commit();
            navigationView.setCheckedItem(R.id.menu_alldocs);
        }
    }

    public void searchPdfInExternalStorage(File folder) {
        if (folder != null) {
            if (folder.listFiles() != null) {
                for (File file : folder.listFiles()) {
                    ArrayList<String> MyFiles = new ArrayList<String>();
                    if (file.isFile()) {
                        //.pdf files
                        if (file.getName().contains(".pdf")) {
                            MyFiles.add(file.getName().replace(".pdf",""));
                            MyFiles.add(file.getPath());

                            PdfReader reader = null;
                            try {
                                reader = new PdfReader(String.valueOf(file));
                                //String dimen = PdfTextExtractor.getTextFromPage(reader,1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            int n = reader.getNumberOfPages();
                            reader.close();

                            int pageNum = 0;
                            PdfiumCore pdfiumCore = new PdfiumCore(context);
                            try {
                                PdfDocument pdfDocument = pdfiumCore.newDocument(openFile(file));
                                pdfiumCore.openPage(pdfDocument, pageNum);
                                int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum);
                                int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum);
                                // ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
                                // RGB_565 - little worse quality, twice less memory usage
                                Bitmap bitmap = Bitmap.createBitmap(width , height , Bitmap.Config.ARGB_8888);
                                pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0, width, height);
                                //if you need to render annotations and form fields, you can use
                                //the same method above adding 'true' as last param
                                pdfiumCore.closeDocument(pdfDocument); // important!
                                preview.add(bitmap);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            MyFiles.add(String.valueOf(n));

                            BasicFileAttributes attrs;
                            try {
                                attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                                FileTime time = attrs.creationTime();

                                String pattern = "yyyy-MM-dd HH:mm:ss";
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                                String formatted = simpleDateFormat.format( new Date( time.toMillis() ) );
                                MyFiles.add(formatted);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            pdfdata.add(MyFiles);
                            Log.d("filePath-------", "" + file.getPath());
                        }
                    } else {
                        searchPdfInExternalStorage(file);
                    }
                }
            }
        }
    }

    public static ParcelFileDescriptor openFile(File file) {
        ParcelFileDescriptor descriptor;
        try {
            descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return descriptor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_bar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.search_topbar:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.import_fg_topbar:
                Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.import_pdf_topbar:
                Toast.makeText(this, "PDF", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.select_topbar:
                Toast.makeText(this, "Select", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.premium_topbar:
                Toast.makeText(this, "Premium", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_alldocs:
                //toolbar.setVisibility(View.VISIBLE);
                floatingActionButton.setAlpha(1f);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_dashboard,new Alldocs()).commit();
                break;
            case R.id.menu_cloud:
                //toolbar.setVisibility(View.GONE);
                floatingActionButton.setAlpha(0f);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_dashboard,new Cloud()).commit();
                break;
            case R.id.menu_shared:
                //toolbar.setVisibility(View.GONE);
                floatingActionButton.setAlpha(0f);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_dashboard,new Shared()).commit();
                break;
            case R.id.menu_tags:
                //toolbar.setVisibility(View.GONE);
                floatingActionButton.setAlpha(0f);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_dashboard,new Tags()).commit();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            int count = getSupportFragmentManager().getBackStackEntryCount();

            if (count == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashBoard.this);
                builder.setTitle("Exit");
                builder.setMessage("Do You Want to Exit")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }
}