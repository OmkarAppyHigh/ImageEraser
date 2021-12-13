package com.appyhigh.imageeraser

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Pair
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

object SaveDrawingTask {

    private const val SAVED_IMAGE_FORMAT = "png"
    private const val SAVED_IMAGE_NAME = "cutout_tmp"

    private fun <R> CoroutineScope.executeAsyncTask(
        onPreExecute: () -> Unit,
        doInBackground: () -> R,
        onPostExecute: (R) -> Unit
    ) = launch {
        onPreExecute()
        val result = withContext(Dispatchers.IO) { // runs in background thread without blocking the Main Thread
            doInBackground()
        }
        onPostExecute(result)
    }

    fun CoroutineScope.saveDrawingTask(activity: EraseActivity, shouldReturnPath: Boolean = false, bitmap: Bitmap) {
        val activityWeakReference: WeakReference<EraseActivity> = WeakReference(activity)
        this.executeAsyncTask(
            onPreExecute = {
                activityWeakReference.get()!!.loadingModal!!.visibility = View.VISIBLE
            },
            doInBackground = {
                try {
                    val file = File.createTempFile(
                        SAVED_IMAGE_NAME,
                        SAVED_IMAGE_FORMAT, activityWeakReference.get()!!.applicationContext.cacheDir
                    )
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                        Pair(file, null)
                    }
                } catch (e: IOException) {
                    Pair(null, e)
                }
            },
            onPostExecute = { result ->
                val resultIntent = Intent()
                if (result.first != null) {
                    if (shouldReturnPath){
                        val file = result.first
                        resultIntent.putExtra(Eraser.ERASER_EXTRA_RESULT_PATH,file?.absolutePath)
                    }else{
                        val uri = Uri.fromFile(result.first)
                        resultIntent.putExtra(Eraser.ERASER_EXTRA_RESULT, uri)
                    }
                    activityWeakReference.get()!!.setResult(Activity.RESULT_OK, resultIntent)
                    activityWeakReference.get()!!.finish()
                } else {
                    activityWeakReference.get()!!.exitWithError(result.second)
                }
            },
        )
    }
}