package com.example.escannrr.uipr.scan

import android.view.Display
import android.view.SurfaceView
import com.example.escannrr.uipr.view.PaperRectangle

interface IScanView {
    interface Proxy {
        fun exit()
        fun getDisplay(): Display
        fun getSurfaceView(): SurfaceView
        fun getPaperRect(): PaperRectangle
    }
}