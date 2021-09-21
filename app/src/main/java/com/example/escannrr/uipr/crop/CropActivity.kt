package com.example.escannrr.uipr.crop

import android.R.attr
import android.content.Intent
import android.graphics.Bitmap
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.example.escannrr.Enhance
import com.example.escannrr.MyApplication
import com.example.escannrr.R
import com.example.escannrr.uipr.base.BaseActivity
import com.example.escannrr.uipr.view.PaperRectangle
import kotlinx.android.synthetic.main.activity_crop.*


class CropActivity : BaseActivity(), ICropView.Proxy {

    private lateinit var mPresenter: CropPresenter

    override fun prepare() {
        nextbtn.setOnClickListener { nextPress() }
        backbtn.setOnClickListener { backPress() }
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun nextAct(bitmap: Bitmap){
        (this.application as MyApplication).setBitmap(bitmap)
        intent = Intent(applicationContext, Enhance::class.java)
        startActivity(intent)
        finish()
    }

    private fun nextPress() {
        mPresenter.crop()
    }

    override fun provideContentViewId(): Int = R.layout.activity_crop

    override fun onBackPressed() {
        backPress()
    }

    fun backPress(){
        val builder = AlertDialog.Builder(this@CropActivity)
        builder.setTitle("Note")
        builder.setMessage("Are you sure you want to discard this image?")
                .setCancelable(false)
                .setPositiveButton("Discard") { dialog, which -> super.onBackPressed() }
                .setNegativeButton("Cancel") { dialog, which -> }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun initPresenter() {
        mPresenter = CropPresenter(this, this,this)
    }

    override fun getPaper(): ImageView = paper

    override fun getPaperRect(): PaperRectangle = paper_rect as PaperRectangle

    override fun getCroppedPaper(): ImageView = picture_cropped
}