package com.example.escannrr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.example.escannrr.uipr.scan.ScanActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SaveAct extends AppCompatActivity implements ClickListener, DragSelectRecyclerViewAdapter.SelectionListener {

    Toolbar toolbar;
    Toolbar btm1,top1;
    String docname;
    LinearLayout share_btn,savetog_btn,move_btn,delete_btn;
    private DragSelectRecyclerView recyclerView;
    private boolean selectionMode = false;
    private String TAG = "ESCANRR";
    private FloatingActionButton floatingActionButton;

    //--------------------------------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onClick(int index) {
        if (selectionMode) {
            myThumbAdapter.toggleSelected(index);
        } else {
            ((MyApplication)this.getApplication()).setBitmap(((MyApplication) this.getApplication()).getDetail().get(index));
            Intent i = new Intent(this, Showtemp.class);
            i.putExtra("position", index);
            this.startActivity(i);
        }
    }

    @Override
    public void onLongClick(int index) {
        if (!selectionMode) {
            setSelectionMode(true);
        }
        recyclerView.setDragSelectActive(true, index);
    }

    @SuppressLint("RestrictedApi")
    private void setSelectionMode(boolean selectionMode) {
        if (selectionMode){
            top1.setVisibility(View.VISIBLE);
            btm1.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.GONE);
            floatingActionButton.setVisibility(View.GONE);
        }
        else {
            top1.setVisibility(View.GONE);
            btm1.setVisibility(View.GONE);
            toolbar.setVisibility(View.VISIBLE);
            floatingActionButton.setVisibility(View.VISIBLE);
        }
        this.selectionMode = selectionMode;
    }

    @Override
    public void onDragSelectionChanged(int i) {
        Log.d(TAG, "DragSelectionChanged: "+i);
        setSelectionMode(i>0);
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------------------------------------

    @SuppressLint("ResourceAsColor")
    public void saveNewPdf(){

        ((MyApplication) getApplication()).setSavedstate(false);

        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Escanrr");

        if (!folder.exists()) {
            folder.mkdir();
            Log.d("TAG", "wrote: created folder " + folder.getPath());
        }

        ArrayList<Bitmap> bitmaps = ((MyApplication) getApplication()).getDetail();

        android.graphics.pdf.PdfDocument pdfDocument = new android.graphics.pdf.PdfDocument();

        for (int i=1;i<bitmaps.size()+1;i++){
            Bitmap bitmap = bitmaps.get(i-1);
            Bitmap finbit = resize(bitmap,960,1280);
            android.graphics.pdf.PdfDocument.PageInfo myPageinfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(960,1280,i).create();
            PdfDocument.Page page = pdfDocument.startPage(myPageinfo);

            Paint paint = new Paint();
            paint.setColor(R.color.white);

            String dimen = finbit.getWidth() + String.valueOf(finbit.getHeight());
            page.getCanvas().drawText(dimen,480,640,paint);

            int cx = (960 - finbit.getWidth()) >> 1;
            int cy = (1280 - finbit.getHeight()) >> 1;

            page.getCanvas().drawBitmap(finbit,cx,cy,null);

            pdfDocument.finishPage(page);
        }

        String pdfFile = ((MyApplication)getApplication()).getPath();
        File myFile = new File(pdfFile);

        try {
            pdfDocument.writeTo(new FileOutputStream(myFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------------------------------------

    public class ThumbAdapter extends DragSelectRecyclerViewAdapter<ThumbAdapter.ThumbViewHolder> {

        private final ClickListener mCallback;
        ArrayList<Bitmap> itemList = new ArrayList<>();

        // Constructor takes click listener callback
        protected ThumbAdapter(SaveAct activity) {
            super();
            mCallback = activity;

            for (Bitmap file : ((MyApplication) activity.getApplication()).getDetail()){
                add(file);
            }
            setSelectionListener(activity);
        }

        void add(Bitmap path){
            itemList.add(path);
        }

        @Override
        public ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
            return new ThumbViewHolder(v);
        }

        //---------------- on bind view holder -------------------

        @Override
        public void onBindViewHolder(ThumbViewHolder holder, int position) {
            super.onBindViewHolder(holder, position); // this line is important!

            Bitmap bitmap = itemList.get(position);
            holder.image.setImageBitmap(bitmap);
            String pos = String.valueOf(position+1);
            holder.number_text.setText(pos);

            if (isIndexSelected(position)) {
                holder.image.setColorFilter(Color.argb(140, 15, 197, 164));
            } else {
                holder.image.setColorFilter(Color.argb(0, 0, 0, 0));
            }
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public ArrayList<Bitmap> getSelectedFiles() {

            ArrayList<Bitmap> selection = new ArrayList<>();

            for ( Integer i: getSelectedIndices() ) {
                selection.add(itemList.get(i));
            }
            return selection;
        }

        //---------------------------- thumb adapter viewholder for my images -----------------------------

        public class ThumbViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

            public final ImageView image;
            public final TextView number_text;

            public ThumbViewHolder(View itemView) {
                super(itemView);
                this.image = (ImageView) itemView.findViewById(R.id.gallery_image);
                this.number_text = (TextView) itemView.findViewById(R.id.number_text_pages);
                this.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                this.itemView.setOnClickListener(this);
                this.itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                // Forwards to the adapter's constructor callback
                if (mCallback != null) mCallback.onClick(getAdapterPosition());
            }

            @Override
            public boolean onLongClick(View v) {
                // Forwards to the adapter's constructor callback
                if (mCallback != null) mCallback.onLongClick(getAdapterPosition());
                return true;
            }
        }
    }

    ThumbAdapter myThumbAdapter;

    //--------------------------------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------------------------------------

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        top1 = findViewById(R.id.toolbar_saveact_select_top);
        btm1 = findViewById(R.id.toolbar_saveact_select_btm);
        share_btn = findViewById(R.id.share_btn);
        move_btn = findViewById(R.id.move_btn);
        savetog_btn = findViewById(R.id.savetog_btn);
        delete_btn = findViewById(R.id.delete_btn);

        myThumbAdapter = new ThumbAdapter(this);

        floatingActionButton = findViewById(R.id.floating_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SaveAct.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = (DragSelectRecyclerView) findViewById(R.id.dynamic_grid_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(myThumbAdapter);

        toolbar = findViewById(R.id.toolbar_saveact);
        docname = ((MyApplication) this.getApplication()).getDocname();

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setTitle(docname);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MyApplication)getApplication()).getSavedstate()){
                    saveNewPdf();
                }
                ((MyApplication) getApplication()).setDetail(null);
                ((MyApplication) getApplication()).setPath(null);
                ((MyApplication) getApplication()).setDocname(null);
                Intent intent = new Intent(SaveAct.this,DashBoard.class);
                startActivity(intent);
                finish();
            }
        });

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Bitmap> files = myThumbAdapter.getSelectedFiles();
                Log.d("CONFIG ---- ","number of items selected  = "+files.size());
                for ( Integer i: myThumbAdapter.getSelectedIndices() ) {
                    Log.d("CONFIG ---- ","indices of item selected  = "+i);
                }
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for ( Integer i: myThumbAdapter.getSelectedIndices() ) {
                    myThumbAdapter.itemList.remove(i);
                    ArrayList<Bitmap> tempimgs = ((MyApplication)getApplication()).getDetail();
                    tempimgs.remove(i);
                    ((MyApplication)getApplication()).setDetail(tempimgs);
                    recyclerView.removeViewAt(i);
                    myThumbAdapter.notifyItemRemoved(i);
                    myThumbAdapter.notifyItemRangeChanged(i, myThumbAdapter.itemList.size());
                }
            }
        });

        move_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SaveAct.this, "Still Left", Toast.LENGTH_SHORT).show();
            }
        });

        savetog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SaveAct.this, "Still Left", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (((MyApplication)getApplication()).getSavedstate()){
            saveNewPdf();
        }
        ((MyApplication) getApplication()).setDetail(null);
        ((MyApplication) getApplication()).setPath(null);
        ((MyApplication) getApplication()).setDocname(null);
        Intent intent = new Intent(SaveAct.this,DashBoard.class);
        startActivity(intent);
        finish();
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------------------------------------

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu_bar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.share_topbar:
                ArrayList<String> pdfs = new ArrayList<>();
                pdfs.add(((MyApplication) getApplication()).getPath());
                ((MyApplication) getApplication()).setSharepdf(pdfs);
                ArrayList<Bitmap> photos = new ArrayList<>(((MyApplication) getApplication()).getDetail());
                ((MyApplication) getApplication()).setShareimages(photos);
                Intent intent = new Intent(SaveAct.this,ShareAct_1.class);
                startActivity(intent);
                return true;
            case R.id.more_options:
                Intent intentm = new Intent(SaveAct.this,MoreAct.class);
                startActivity(intentm);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void shareImages() {
        final Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("image/jpg");

        ArrayList<Uri> filesUris = new ArrayList<>();
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesUris);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_snackbar)));
    }
}