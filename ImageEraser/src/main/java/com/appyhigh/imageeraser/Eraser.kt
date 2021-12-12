package com.appyhigh.imageeraser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.NonNull
import androidx.annotation.Nullable


object Eraser {
    const val CUTOUT_ACTIVITY_REQUEST_CODE: Int = 368
    const val CUTOUT_ACTIVITY_RESULT_ERROR_CODE: Int = 3680
    const val CUTOUT_EXTRA_SOURCE = "CUTOUT_EXTRA_SOURCE"
    const val CUTOUT_EXTRA_RESULT = "CUTOUT_EXTRA_RESULT"
    const val CUTOUT_EXTRA_RESULT_PATH_ENABLED = "CUTOUT_EXTRA_RESULT_PATH_ENABLED"
    const val CUTOUT_EXTRA_RESULT_PATH = "CUTOUT_EXTRA_RESULT_PATH"


    fun activity(): ActivityBuilder {
        return ActivityBuilder()
    }

    /**
     * Reads the [android.net.Uri] from the result data. This Uri is the path to the saved PNG
     *
     * @param data Result data to get the Uri from
     */
    fun getUri(@Nullable data: Intent?): Uri? {
        return data?.getParcelableExtra(CUTOUT_EXTRA_RESULT)
    }

    /**
     * Reads the [android.net.Uri] from the result data. This Uri is the path to the saved PNG
     *
     * @param data Result data to get the Uri from
     */
    fun getResultPath(@Nullable data: Intent?): String? {
        return data?.getStringExtra(CUTOUT_EXTRA_RESULT_PATH)
    }

    /**
     * Gets an Exception from the result data if the [EraseActivity] failed at some point
     *
     * @param data Result data to get the Exception from
     */
    fun getError(@Nullable data: Intent?): Exception? {
        return if (data != null) data.getSerializableExtra(CUTOUT_EXTRA_RESULT) as Exception? else null
    }

    /**
     * Builder used for creating CutOut Activity by user request.
     */
    class ActivityBuilder() {

        private var shouldReturnResultPath: Boolean = false

        @Nullable
        private var source // The image to crop source Android uri
                : Uri? = null

        /**
         * Get [EraseActivity] intent to start the activity.
         */
        private fun getIntent(@NonNull context: Context): Intent {
            val intent = Intent()
            intent.setClass(context, EraseActivity::class.java)
            if (source != null) {
                intent.putExtra(CUTOUT_EXTRA_SOURCE, source)
            }
            if (shouldReturnResultPath) {
                intent.putExtra(CUTOUT_EXTRA_RESULT_PATH_ENABLED, true)
            }
            return intent
        }

        /**
         * By default the user can select images from camera or gallery but you can also call this method to load a pre-saved image
         *
         * @param source [android.net.Uri] instance of the image to be loaded
         */
        fun src(source: Uri?): ActivityBuilder {
            this.source = source
            return this
        }

        /**
         * By default library will return uri of the result image. If shouldReturnResultPath is true library return file path
         *
         * @param shouldReturnResultPath [Boolean] Boolean
         */
        fun shouldReturnResultPath(shouldReturnResultPath: Boolean): ActivityBuilder {
            this.shouldReturnResultPath = shouldReturnResultPath
            return this
        }

        /**
         * Start [EraseActivity].
         *
         * @param activity activity to receive result
         */
        fun start(activity: Activity) {
            activity.startActivityForResult(
                getIntent(activity),
                CUTOUT_ACTIVITY_REQUEST_CODE
            )
        }
    }
}
