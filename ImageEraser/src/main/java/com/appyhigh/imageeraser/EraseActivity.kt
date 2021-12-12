package com.appyhigh.imageeraser

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alexvasilkov.gestures.views.interfaces.GestureView
import com.appyhigh.imageeraser.SaveDrawingTask.saveDrawingTask
import top.defaults.checkerboarddrawable.CheckerboardDrawable
import java.io.IOException
import java.lang.Exception


class EraseActivity : AppCompatActivity() {
    var loadingModal: FrameLayout? = null
    private var gestureView: GestureView? = null
    private var drawView: DrawView? = null
    private var manualClearSettingsLayout: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit_new)

//        Toolbar toolbar = findViewById(R.id.photo_edit_toolbar);
//        toolbar.setBackgroundColor(Color.BLACK);
//        toolbar.setTitleTextColor(Color.WHITE);
//        setSupportActionBar(toolbar);
        val drawViewLayout: FrameLayout = findViewById(R.id.drawViewLayout)
        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            drawViewLayout.setBackgroundDrawable(CheckerboardDrawable.create())
        } else {
            drawViewLayout.background = CheckerboardDrawable.create()
        }
        val strokeBar: SeekBar = findViewById(R.id.strokeBar)
        strokeBar.max = MAX_ERASER_SIZE.toInt()
        strokeBar.progress = 50
        gestureView = findViewById(R.id.gestureView)
        drawView = findViewById(R.id.drawView)
        drawView?.isDrawingCacheEnabled = true
        drawView?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        //drawView.setDrawingCacheEnabled(true);
        drawView?.setStrokeWidth(strokeBar.progress)
        strokeBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                drawView!!.setStrokeWidth(seekBar.progress)
            }
        })
        loadingModal = findViewById(R.id.loadingModal)
        loadingModal?.visibility = View.INVISIBLE
        drawView!!.setLoadingModal(loadingModal)
        manualClearSettingsLayout = findViewById(R.id.manual_clear_settings_layout)
        setUndoRedo()
        initializeActionButtons()
        val doneButton: Button = findViewById(R.id.done)
        doneButton.setOnClickListener { v: View? -> startSaveDrawingTask() }

        start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(RESULT_CANCELED)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val extraSource: Uri?
        get() = if (intent.hasExtra(Eraser.CUTOUT_EXTRA_SOURCE)) intent.getParcelableExtra(
            Eraser.CUTOUT_EXTRA_SOURCE
        ) else null

    private fun start() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val uri = extraSource
            if (uri != null) {
                setDrawViewBitmap(uri)
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_CODE
            )
        }
    }

    private fun startSaveDrawingTask() {
        val shouldReturnPath = intent?.getBooleanExtra(Eraser.CUTOUT_EXTRA_RESULT_PATH_ENABLED,false) ?: false
        drawView?.drawingCache?.let {
            lifecycleScope.saveDrawingTask(this,shouldReturnPath,it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            start()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun activateGestureView() {
        gestureView?.controller?.settings
            ?.setMaxZoom(MAX_ZOOM)
            ?.setDoubleTapZoom(-1f) // Falls back to max zoom level
            ?.setPanEnabled(true)
            ?.setZoomEnabled(true)
            ?.setDoubleTapEnabled(true)
            ?.setOverscrollDistance(0f, 0f)?.overzoomFactor = 2f
    }

    private fun deactivateGestureView() {
        gestureView?.controller?.settings
            ?.setPanEnabled(false)
            ?.setZoomEnabled(false)?.isDoubleTapEnabled = false
    }

    private fun initializeActionButtons() {
        val autoClearButton: Button = findViewById(R.id.auto_clear_button)
        val manualClearButton: Button = findViewById(R.id.manual_clear_button)
        val zoomButton: Button = findViewById(R.id.zoom_button)
        autoClearButton.isActivated = false
        autoClearButton.setOnClickListener { buttonView: View? ->
            if (!autoClearButton.isActivated) {
                drawView?.setAction(DrawView.DrawViewAction.AUTO_CLEAR)
                manualClearSettingsLayout!!.visibility = View.INVISIBLE
                autoClearButton.isActivated = true
                manualClearButton.isActivated = false
                zoomButton.isActivated = false
                deactivateGestureView()
            }
        }
        manualClearButton.isActivated = true
        drawView?.setAction(DrawView.DrawViewAction.MANUAL_CLEAR)
        manualClearButton.setOnClickListener { buttonView: View? ->
            if (!manualClearButton.isActivated) {
                drawView?.setAction(DrawView.DrawViewAction.MANUAL_CLEAR)
                manualClearSettingsLayout!!.visibility = View.VISIBLE
                manualClearButton.isActivated = true
                autoClearButton.isActivated = false
                zoomButton.isActivated = false
                deactivateGestureView()
            }
        }
        zoomButton.isActivated = false
        deactivateGestureView()
        zoomButton.setOnClickListener { buttonView: View? ->
            if (!zoomButton.isActivated) {
                drawView?.setAction(DrawView.DrawViewAction.ZOOM)
                manualClearSettingsLayout!!.visibility = View.INVISIBLE
                zoomButton.isActivated = true
                manualClearButton.isActivated = false
                autoClearButton.isActivated = false
                activateGestureView()
            }
        }
    }

    private fun setUndoRedo() {
        val undoButton: Button = findViewById(R.id.undo)
        undoButton.isEnabled = false
        undoButton.setOnClickListener { v: View? -> undo() }
        val redoButton: Button = findViewById(R.id.redo)
        redoButton.isEnabled = false
        redoButton.setOnClickListener { v: View? -> redo() }
        drawView?.setButtons(undoButton, redoButton)
    }

    fun exitWithError(e: Exception?) {
        val intent = Intent()
        intent.putExtra(Eraser.CUTOUT_EXTRA_RESULT, e)
        setResult(Eraser.CUTOUT_ACTIVITY_RESULT_ERROR_CODE, intent)
        finish()
    }

    private fun setDrawViewBitmap(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            drawView?.setBitmap(bitmap)
        } catch (e: IOException) {
            exitWithError(e)
        }
    }

    private fun undo() {
        drawView?.undo()
    }

    private fun redo() {
        drawView?.redo()
    }

    companion object {
        private const val WRITE_EXTERNAL_STORAGE_CODE = 1
        private const val MAX_ERASER_SIZE: Short = 150
        private const val MAX_ZOOM = 4f
    }
}