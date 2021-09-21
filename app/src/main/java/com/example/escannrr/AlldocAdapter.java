package com.example.escannrr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.languages.ArabicLigaturizer;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AlldocAdapter extends RecyclerView.Adapter<AlldocAdapter.AlldocViewholder> {

    public ArrayList<ArrayList<String>> pdfdata;
    public ArrayList<Bitmap> preview;
    public Context context;

    public AlldocAdapter(ArrayList<ArrayList<String>> pdfdata, Context context){
        this.pdfdata = pdfdata;
        this.preview = ((MyApplication) context.getApplicationContext()).getPreview();
        this.context = context;
    }

    @NonNull
    @Override
    public AlldocViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_alldoc,parent,false);
        return new AlldocViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlldocViewholder holder, int position) {
        ArrayList<String> data = pdfdata.get(position);
        holder.textView.setText(data.get(0));
        ((MyApplication) context.getApplicationContext()).setDocname(data.get(0));
        holder.imageView.setImageBitmap(preview.get(position));
        holder.number_pages.setText(data.get(2));
        String[] strings = data.get(3).trim().split(" ");
        String[] dates = strings[0].trim().split("-");
        String cdate = dates[2]+"-"+dates[1]+"-"+dates[0];
        holder.date_creation.setText(cdate);
        String path = data.get(1);
        holder.main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MyApplication) context.getApplicationContext()).setDocname(data.get(0));

                ArrayList<String> dimens = new ArrayList<>();
                ArrayList<Bitmap> bitmaps = new ArrayList<>();
                int n;

                PdfReader reader = null;
                try {
                    reader = new PdfReader(path);
                    n = reader.getNumberOfPages();
                    for (int i=0;i<n;i++){
                        String str = PdfTextExtractor.getTextFromPage(reader,i+1);
                        if (str.equals("") || str == null) dimens.add("9601280");
                        else dimens.add(str);
                        Log.d("Tag =  dimen org = ","  total = "+PdfTextExtractor.getTextFromPage(reader,i+1));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                n = reader.getNumberOfPages();
                reader.close();

                for (int j=0;j<n;j++){
                    PdfiumCore pdfiumCore = new PdfiumCore(context);
                    try {
                        File file = new File(path);
                        String h = dimens.get(j).substring(3);
                        String w = dimens.get(j).substring(0,3);
                        Log.d("TAG ---------- Dimen = "," width = "+w+ " height = " + h);
                        PdfDocument pdfDocument = pdfiumCore.newDocument(openFile(file));
                        pdfiumCore.openPage(pdfDocument, j);
                        int width = pdfiumCore.getPageWidthPoint(pdfDocument, j);//Integer.parseInt(w);
                        int height = pdfiumCore.getPageHeightPoint(pdfDocument, j);//Integer.parseInt(h);
                        // ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
                        // RGB_565 - little worse quality, twice less memory usage
                        Bitmap bitmap = Bitmap.createBitmap(width , height , Bitmap.Config.ARGB_8888);
                        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, j, 0, 0, width, height);
                        //if you need to render annotations and form fields, you can use
                        //the same method above adding 'true' as last param
                        pdfiumCore.closeDocument(pdfDocument); // important!
                        Bitmap finbit = resizeBit(bitmap,w,h);
                        Log.d("Tag == Final bit size = "," width = "+finbit.getWidth()+" height = "+finbit.getHeight());
                        bitmaps.add(finbit);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                ((MyApplication) context.getApplicationContext()).setDetail(bitmaps);

                Intent intent = new Intent(context,SaveAct.class);
                context.startActivity(intent);
            }
        });
    }

    private Bitmap resizeBit(Bitmap bitmap, String w, String h) {
        Bitmap finbit = null;
        int fw = Integer.parseInt(w);
        int fh = Integer.parseInt(h);
        int cx = (960 - fw) >> 1;
        int cy = (1280 - fh) >> 1;
        finbit = Bitmap.createBitmap(bitmap,cx,cy,fw,fh);
        return finbit;
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
    public int getItemCount() {
        return pdfdata.size();
    }

    public class AlldocViewholder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView;
        RelativeLayout main_layout;
        TextView date_creation;
        TextView number_pages;

        public AlldocViewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_pdf_front);
            textView = itemView.findViewById(R.id.pdf_title_front);
            main_layout = itemView.findViewById(R.id.main_layout);
            date_creation = itemView.findViewById(R.id.date_created);
            number_pages = itemView.findViewById(R.id.number_of_pages);
        }
    }

}
