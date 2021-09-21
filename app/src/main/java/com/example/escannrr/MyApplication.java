package com.example.escannrr;

import android.app.Application;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class MyApplication extends Application {

    public Bitmap bitmap;
    public String docname;
    public ArrayList<Bitmap> preview;
    public ArrayList<Bitmap> detail;
    public Boolean savedstate = false;
    public String path;
    public String clmname;
    public ArrayList<Bitmap> shareimages;
    public ArrayList<String> sharepdf;

    public Bitmap getBitmap(){
        return bitmap;
    }
    public String getDocname() { return docname; }
    public ArrayList<Bitmap> getPreview() { return preview; }
    public ArrayList<Bitmap> getDetail() { return detail; }
    public Boolean getSavedstate() { return savedstate; }
    public String getPath() { return path; }
    public String getClmname() { return clmname; }
    public ArrayList<Bitmap> getShareimages() { return shareimages; }
    public ArrayList<String> getSharepdf() { return sharepdf; }

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }
    public void setDocname(String docname) { this.docname = docname; }
    public void setPreview(ArrayList<Bitmap> preview) { this.preview = preview; }
    public void setDetail(ArrayList<Bitmap> detail ) { this.detail = detail; }
    public void setSavedstate(Boolean savedstate) {this.savedstate = savedstate; }
    public void setPath(String path) { this.path = path; }
    public void setClmname(String clmname) { this.clmname = clmname; }
    public void setShareimages(ArrayList<Bitmap> shareimages) { this.shareimages = shareimages; }
    public void setSharepdf(ArrayList<String> sharepdf) { this.sharepdf = sharepdf; }
}
