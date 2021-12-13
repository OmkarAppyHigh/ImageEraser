package com.appyhigh.demo

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.appyhigh.imageeraser.Eraser
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var ivImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivImage = findViewById(R.id.image)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab)
        val testImageUri: Uri? = getUriFromDrawable(R.drawable.test_image)

        fabAdd.setOnClickListener {
            testImageUri?.let { uri ->
                Eraser
                    .activity()
                    .src(uri)
                    .setImageRatio("1:1")
                    .shouldReturnResultPath(true)
                    .start(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Eraser.ERASER_ACTIVITY_REQUEST_CODE){
            when(resultCode){
                Activity.RESULT_OK -> {
                    val path = Eraser.getResultPath(data)
                    Log.d("EraseResult",path.toString())
                    val bitmap = BitmapFactory.decodeFile(path)
                    bitmap?.let { ivImage?.setImageBitmap(it) } ?: run {
                        Log.d("EraseResult","bitmap is null") }
                }
                Eraser.ERASER_ACTIVITY_RESULT_ERROR_CODE -> {
                    Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getUriFromDrawable(drawableId: Int): Uri? {
        return Uri.parse("android.resource://$packageName/drawable/" + applicationContext.resources.getResourceEntryName(drawableId))
    }
}