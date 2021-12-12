package com.appyhigh.imageeraser

import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import java.lang.ref.WeakReference
import java.util.*

internal class DrawView(c: Context?, attrs: AttributeSet?) :
    View(c, attrs) {
    private var livePath: Path
    private var pathPaint: Paint
    var currentBitmap: Bitmap? = null
        private set
    private val cuts = Stack<Pair<Pair<Path, Paint>?, Bitmap?>>()
    private val undoneCuts = Stack<Pair<Pair<Path, Paint>?, Bitmap?>>()
    private var pathX = 0f
    private var pathY = 0f
    private var undoButton: Button? = null
    private var redoButton: Button? = null
    private var loadingModal: View? = null
    private var currentAction: DrawViewAction? = null

    enum class DrawViewAction {
        AUTO_CLEAR, MANUAL_CLEAR, ZOOM
    }

    fun setButtons(undoButton: Button?, redoButton: Button?) {
        this.undoButton = undoButton
        this.redoButton = redoButton
    }

    override fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)
        resizeBitmap(newWidth, newHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        if (currentBitmap != null) {
            canvas.drawBitmap(currentBitmap!!, 0f, 0f, null)
            for (action in cuts) {
                if (action.first != null) {
                    canvas.drawPath(action.first!!.first, action.first!!.second)
                }
            }
            if (currentAction == DrawViewAction.MANUAL_CLEAR) {
                canvas.drawPath(livePath, pathPaint)
            }
        }
        canvas.restore()
    }

    private fun touchStart(x: Float, y: Float) {
        pathX = x
        pathY = y
        undoneCuts.clear()
        redoButton!!.isEnabled = false
        if (currentAction == DrawViewAction.AUTO_CLEAR) {
            AutomaticPixelClearingTask(this).execute(
                x.toInt(),
                y.toInt()
            )
        } else {
            livePath.moveTo(x, y)
        }
        invalidate()
    }

    private fun touchMove(x: Float, y: Float) {
        if (currentAction == DrawViewAction.MANUAL_CLEAR) {
            val dx = Math.abs(x - pathX)
            val dy = Math.abs(y - pathY)
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                livePath.quadTo(pathX, pathY, (x + pathX) / 2, (y + pathY) / 2)
                pathX = x
                pathY = y
            }
        }
    }

    private fun touchUp() {
        if (currentAction == DrawViewAction.MANUAL_CLEAR) {
            livePath.lineTo(pathX, pathY)
            cuts.push(Pair(Pair(livePath, pathPaint), null))
            livePath = Path()
            undoButton!!.isEnabled = true
        }
    }

    fun undo() {
        if (cuts.size > 0) {
            val cut = cuts.pop()
            if (cut.second != null) {
                undoneCuts.push(
                    Pair(
                        null,
                        currentBitmap
                    )
                )
                currentBitmap = cut.second
            } else {
                undoneCuts.push(cut)
            }
            if (cuts.isEmpty()) {
                undoButton!!.isEnabled = false
            }
            redoButton!!.isEnabled = true
            invalidate()
        }
        //toast the user
    }

    fun redo() {
        if (undoneCuts.size > 0) {
            val cut = undoneCuts.pop()
            if (cut.second != null) {
                cuts.push(
                    Pair(
                        null,
                        currentBitmap
                    )
                )
                currentBitmap = cut.second
            } else {
                cuts.push(cut)
            }
            if (undoneCuts.isEmpty()) {
                redoButton!!.isEnabled = false
            }
            undoButton!!.isEnabled = true
            invalidate()
        }
        //toast the user
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (currentBitmap != null && currentAction != DrawViewAction.ZOOM) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStart(ev.x, ev.y)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    touchMove(ev.x, ev.y)
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    touchUp()
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun resizeBitmap(width: Int, height: Int) {
        if (width > 0 && height > 0 && currentBitmap != null) {
            currentBitmap = BitmapUtility.getResizedBitmap(currentBitmap!!, width, height)
            currentBitmap!!.setHasAlpha(true)
            invalidate()
        }
    }

    fun setBitmap(bitmap: Bitmap?) {
        currentBitmap = bitmap
        resizeBitmap(width, height)
    }

    fun setAction(newAction: DrawViewAction?) {
        currentAction = newAction
    }

    fun setStrokeWidth(strokeWidth: Int) {
        pathPaint = Paint(pathPaint)
        pathPaint.strokeWidth = strokeWidth.toFloat()
    }

    fun setLoadingModal(loadingModal: View?) {
        this.loadingModal = loadingModal
    }

    private class AutomaticPixelClearingTask(drawView: DrawView) :
        AsyncTask<Int?, Void?, Bitmap>() {
        private val drawViewWeakReference: WeakReference<DrawView> = WeakReference(drawView)
        override fun onPreExecute() {
            super.onPreExecute()
            drawViewWeakReference.get()!!.loadingModal!!.visibility = VISIBLE
            drawViewWeakReference.get()!!.cuts.push(
                Pair(
                    null,
                    drawViewWeakReference.get()!!.currentBitmap
                )
            )
        }

        override fun doInBackground(vararg points: Int?): Bitmap? {
            val oldBitmap = drawViewWeakReference.get()!!.currentBitmap
            val colorToReplace = oldBitmap!!.getPixel(points[0]!!, points[1]!!)
            val width = oldBitmap.width
            val height = oldBitmap.height
            val pixels = IntArray(width * height)
            oldBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val rA = Color.alpha(colorToReplace)
            val rR = Color.red(colorToReplace)
            val rG = Color.green(colorToReplace)
            val rB = Color.blue(colorToReplace)
            var pixel: Int

            // iteration through pixels
            for (y in 0 until height) {
                for (x in 0 until width) {
                    // get current index in 2D-matrix
                    val index = y * width + x
                    pixel = pixels[index]
                    val rrA = Color.alpha(pixel)
                    val rrR = Color.red(pixel)
                    val rrG = Color.green(pixel)
                    val rrB = Color.blue(pixel)
                    if (rA - COLOR_TOLERANCE < rrA && rrA < rA + COLOR_TOLERANCE && rR - COLOR_TOLERANCE < rrR && rrR < rR + COLOR_TOLERANCE && rG - COLOR_TOLERANCE < rrG && rrG < rG + COLOR_TOLERANCE && rB - COLOR_TOLERANCE < rrB && rrB < rB + COLOR_TOLERANCE) {
                        pixels[index] = Color.TRANSPARENT
                    }
                }
            }
            val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            newBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return newBitmap
        }

        override fun onPostExecute(result: Bitmap) {
            super.onPostExecute(result)
            drawViewWeakReference.get()!!.currentBitmap = result
            drawViewWeakReference.get()!!.undoButton!!.isEnabled = true
            drawViewWeakReference.get()!!.loadingModal!!.visibility = INVISIBLE
            drawViewWeakReference.get()!!.invalidate()
        }

    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
        private const val COLOR_TOLERANCE = 20f
    }

    init {
        livePath = Path()
        pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        pathPaint.isDither = true
        pathPaint.color = Color.TRANSPARENT
        pathPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        pathPaint.style = Paint.Style.STROKE
        pathPaint.strokeJoin = Paint.Join.ROUND
        pathPaint.strokeCap = Paint.Cap.ROUND
    }
}