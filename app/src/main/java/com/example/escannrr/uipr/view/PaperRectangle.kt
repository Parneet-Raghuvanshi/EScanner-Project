package com.example.escannrr.uipr.view

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.escannrr.R
import com.example.escannrr.uipr.SourceManager
import com.example.escannrr.uipr.processor.Corners
import com.example.escannrr.uipr.processor.TAG
import com.example.escannrr.uipr.scan.ScanActivity
import org.opencv.core.Point
import org.opencv.core.Size

class PaperRectangle : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, defTheme: Int) : super(context, attributes, defTheme)

    private val rectPaint = Paint()
    private val circlePaint = Paint()
    private var ratioX: Double = 1.0
    private var ratioY: Double = 1.0
    private var tl: Point = Point()
    private var tr: Point = Point()
    private var br: Point = Point()
    private var bl: Point = Point()

    //--------new -------------
    private var a: Point = Point()
    private var b: Point = Point()
    private var c: Point = Point()
    private var d: Point = Point()

    private val path: Path = Path()
    private var point2Move = Point()
    private var cropMode = false
    private var latestDownX = 0.0F
    private var latestDownY = 0.0F

    init {
        rectPaint.color = resources.getColor(R.color.colorAccent)
        rectPaint.isAntiAlias = true
        rectPaint.isDither = true
        rectPaint.strokeWidth = 4F
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeJoin = Paint.Join.MITER // set the join to round you want
        rectPaint.strokeCap = Paint.Cap.SQUARE      // set the paint cap to round too
        //rectPaint.pathEffect = CornerPathEffect(10f)

        circlePaint.color = Color.LTGRAY
        circlePaint.isDither = true
        circlePaint.isAntiAlias = true
        circlePaint.strokeWidth = 4F
        circlePaint.style = Paint.Style.STROKE
    }

    fun onCornersDetected(corners: Corners) {
        ratioX = corners.size.width.div(measuredWidth)
        ratioY = corners.size.height.div(measuredHeight)
        tl = corners.corners[0] ?: Point()
        tr = corners.corners[1] ?: Point()
        br = corners.corners[2] ?: Point()
        bl = corners.corners[3] ?: Point()
        //a = corners.corners[4] ?: Point()
        //b = corners.corners[5] ?: Point()
        //c = corners.corners[6] ?: Point()
        //d = corners.corners[7] ?: Point()

        Log.i(TAG, "POINTS ------>  ${tl.toString()} corners")

        resize()
        path.reset()
        path.moveTo(tl.x.toFloat(), tl.y.toFloat())
        //path.lineTo((tl.x.toFloat()+tr.x.toFloat())/2,(tl.y.toFloat()+tr.y.toFloat())/2)
        path.lineTo(tr.x.toFloat(), tr.y.toFloat())
        //path.lineTo((tr.x.toFloat()+br.x.toFloat())/2,(tr.y.toFloat()+br.y.toFloat())/2)
        path.lineTo(br.x.toFloat(), br.y.toFloat())
        //path.lineTo((br.x.toFloat()+bl.x.toFloat())/2,(br.y.toFloat()+bl.y.toFloat())/2)
        path.lineTo(bl.x.toFloat(), bl.y.toFloat())
        //path.lineTo((bl.x.toFloat()+tl.x.toFloat())/2,(bl.y.toFloat()+tl.y.toFloat())/2)
        path.close()
        invalidate()
    }

    fun onCornersNotDetected() {
        path.reset()
        invalidate()
    }

    fun onCorners2Crop(corners: Corners?, size: Size?) {

        cropMode = true
        tl = corners?.corners?.get(0) ?: SourceManager.defaultTl
        tr = corners?.corners?.get(1) ?: SourceManager.defaultTr
        br = corners?.corners?.get(2) ?: SourceManager.defaultBr
        bl = corners?.corners?.get(3) ?: SourceManager.defaultBl
        //a = corners?.corners?.get(4) ?: SourceManager.defaultA
        //b = corners?.corners?.get(5) ?: SourceManager.defaultB
        //c = corners?.corners?.get(6) ?: SourceManager.defaultC
        //d = corners?.corners?.get(7) ?: SourceManager.defaultD

        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        //exclude status bar height
        val statusBarHeight = getStatusBarHeight(context)
        ratioX = size?.width?.div(displayMetrics.widthPixels) ?: 1.0
        ratioY = size?.height?.div(displayMetrics.heightPixels - statusBarHeight) ?: 1.0
        resize()
        movePoints()
    }

    fun getCorners2Crop(): List<Point> {
        reverseSize()
        return listOf(tl, tr, br, bl)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(path, rectPaint)
        if (cropMode) {
            canvas?.drawCircle(tl.x.toFloat(), tl.y.toFloat(), 25F, circlePaint)
            //canvas?.drawCircle((tl.x.toFloat()+tr.x.toFloat())/2,(tl.y.toFloat()+tr.y.toFloat())/2, 20F, circlePaint)
            canvas?.drawCircle(tr.x.toFloat(), tr.y.toFloat(), 25F, circlePaint)
            //canvas?.drawCircle((tr.x.toFloat()+br.x.toFloat())/2,(tr.y.toFloat()+br.y.toFloat())/2, 20F, circlePaint)
            canvas?.drawCircle(bl.x.toFloat(), bl.y.toFloat(), 25F, circlePaint)
            //canvas?.drawCircle((br.x.toFloat()+bl.x.toFloat())/2,(br.y.toFloat()+bl.y.toFloat())/2, 20F, circlePaint)
            canvas?.drawCircle(br.x.toFloat(), br.y.toFloat(), 25F, circlePaint)
            //canvas?.drawCircle((bl.x.toFloat()+tl.x.toFloat())/2,(bl.y.toFloat()+tl.y.toFloat())/2, 20F, circlePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (!cropMode) {
            return false
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                latestDownX = event.x
                latestDownY = event.y
                calculatePoint2Move(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                point2Move.x = (event.x - latestDownX) + point2Move.x
                point2Move.y = (event.y - latestDownY) + point2Move.y
                movePoints()
                latestDownY = event.y
                latestDownX = event.x
            }
        }
        return true
    }

    private fun calculatePoint2Move(downX: Float, downY: Float) {
        val points = listOf(tl, tr, br, bl)
        point2Move = points.minBy { Math.abs((it.x - downX).times(it.y - downY)) } ?: tl
    }

    private fun movePoints() {
        path.reset()
        path.moveTo(tl.x.toFloat(), tl.y.toFloat())
        //path.lineTo((tl.x.toFloat()+tr.x.toFloat())/2,(tl.y.toFloat()+tr.y.toFloat())/2)
        path.lineTo(tr.x.toFloat(), tr.y.toFloat())
        //path.lineTo((tr.x.toFloat()+br.x.toFloat())/2,(tr.y.toFloat()+br.y.toFloat())/2)
        path.lineTo(br.x.toFloat(), br.y.toFloat())
        //path.lineTo((br.x.toFloat()+bl.x.toFloat())/2,(br.y.toFloat()+bl.y.toFloat())/2)
        path.lineTo(bl.x.toFloat(), bl.y.toFloat())
        //path.lineTo((bl.x.toFloat()+tl.x.toFloat())/2,(bl.y.toFloat()+tl.y.toFloat())/2)
        path.close()
        invalidate()
    }


    private fun resize() {
        tl.x = tl.x.div(ratioX)
        tl.y = tl.y.div(ratioY)
        tr.x = tr.x.div(ratioX)
        tr.y = tr.y.div(ratioY)
        br.x = br.x.div(ratioX)
        br.y = br.y.div(ratioY)
        bl.x = bl.x.div(ratioX)
        bl.y = bl.y.div(ratioY)
        /*a.x = a.x.div(ratioX)
        a.y = a.y.div(ratioY)
        b.x = b.x.div(ratioX)
        b.y = b.y.div(ratioY)
        c.x = c.x.div(ratioX)
        c.y = c.y.div(ratioY)
        d.x = d.x.div(ratioX)
        d.y = d.y.div(ratioY)*/
    }

    private fun reverseSize() {
        tl.x = tl.x.times(ratioX)
        tl.y = tl.y.times(ratioY)
        tr.x = tr.x.times(ratioX)
        tr.y = tr.y.times(ratioY)
        br.x = br.x.times(ratioX)
        br.y = br.y.times(ratioY)
        bl.x = bl.x.times(ratioX)
        bl.y = bl.y.times(ratioY)
        /*a.x = a.x.times(ratioX)
        a.y = a.y.times(ratioY)
        b.x = b.x.times(ratioX)
        b.y = b.y.times(ratioY)
        c.x = c.x.times(ratioX)
        c.y = c.y.times(ratioY)
        d.x = d.x.times(ratioX)
        d.y = d.y.times(ratioY)*/
    }

    private fun getNavigationBarHeight(pContext: Context): Int {
        val resources = pContext.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    private fun getStatusBarHeight(pContext: Context): Int {
        val resources = pContext.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }
}