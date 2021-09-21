package com.example.escannrr.uipr.crop

import android.widget.ImageView
import com.example.escannrr.uipr.view.PaperRectangle

class ICropView {
    interface Proxy {
        fun getPaper(): ImageView
        fun getPaperRect(): PaperRectangle
        fun getCroppedPaper(): ImageView
    }
}