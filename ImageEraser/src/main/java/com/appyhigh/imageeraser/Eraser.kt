package com.appyhigh.imageeraser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.ColorRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable


object Eraser {
    const val ERASER_ACTIVITY_REQUEST_CODE: Int = 368
    const val ERASER_ACTIVITY_RESULT_ERROR_CODE: Int = 3680
    const val ERASER_EXTRA_SOURCE = "ERASER_EXTRA_SOURCE"
    const val ERASER_EXTRA_RESULT = "ERASER_EXTRA_RESULT"
    const val ERASER_EXTRA_RESULT_PATH_ENABLED = "ERASER_EXTRA_RESULT_PATH_ENABLED"
    const val ERASER_EXTRA_RESULT_PATH = "ERASER_EXTRA_RESULT_PATH"
    const val ERASER_EXTRA_BACKGROUND_COLOR = "ERASER_EXTRA_BACKGROUND_COLOR"
    const val ERASER_EXTRA_TOOLBAR_BACKGROUND_COLOR = "ERASER_EXTRA_TOOLBAR_BACKGROUND_COLOR"
    const val ERASER_EXTRA_SEEKBAR_COLOR = "ERASER_EXTRA_SEEKBAR_COLOR"
    const val ERASER_EXTRA_BUTTON_COLOR = "ERASER_EXTRA_BUTTON_COLOR"
    const val ERASER_EXTRA_IMAGE_RATIO = "ERASER_EXTRA_IMAGE_RATIO"


    fun activity(): ActivityBuilder {
        return ActivityBuilder()
    }

    /**
     * Reads the [android.net.Uri] from the result data. This Uri is the path to the saved PNG
     *
     * @param data Result data to get the Uri from
     */
    fun getUri(@Nullable data: Intent?): Uri? {
        return data?.getParcelableExtra(ERASER_EXTRA_RESULT)
    }

    /**
     * Reads the [String] from the result data. This String is the path to the saved PNG
     *
     * @param data Result data to get the file path from
     */
    fun getResultPath(@Nullable data: Intent?): String? {
        return data?.getStringExtra(ERASER_EXTRA_RESULT_PATH)
    }

    /**
     * Gets an Exception from the result data if the [EraseActivity] failed at some point
     *
     * @param data Result data to get the Exception from
     */
    fun getError(@Nullable data: Intent?): Exception? {
        return if (data != null) data.getSerializableExtra(ERASER_EXTRA_RESULT) as Exception? else null
    }

    /**
     * Builder used for creating CutOut Activity by user request.
     */
    class ActivityBuilder() {

        private var shouldReturnResultPath: Boolean = false
        private var backgroundColor: Int = R.color.erase_background_color
        private var toolbarBgColor: Int = R.color.toolbar_background_color
        private var seekBarColor: Int = R.color.button_color
        private var buttonColor: Int = R.color.button_color
        private var ratio: String = ""

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
                intent.putExtra(ERASER_EXTRA_SOURCE, source)
            }
            if (shouldReturnResultPath) {
                intent.putExtra(ERASER_EXTRA_RESULT_PATH_ENABLED, true)
            }
            intent.putExtra(ERASER_EXTRA_BACKGROUND_COLOR,backgroundColor)
            intent.putExtra(ERASER_EXTRA_BUTTON_COLOR,buttonColor)
            intent.putExtra(ERASER_EXTRA_SEEKBAR_COLOR,seekBarColor)
            if (ratio.isNotEmpty()){
                intent.putExtra(ERASER_EXTRA_IMAGE_RATIO,ratio)
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

        fun setBackgroundColor(@ColorRes color: Int) : ActivityBuilder {
            this.backgroundColor = color
            return this
        }

        fun setToolBarBackgroundColor(@ColorRes color: Int) : ActivityBuilder {
            this.toolbarBgColor = color
            return this
        }

        fun setButtonColor(@ColorRes color: Int) : ActivityBuilder {
            this.buttonColor = color
            return this
        }

        fun setSeekbarColor(@ColorRes color: Int) : ActivityBuilder {
            this.seekBarColor = color
            return this
        }

        /**
         * Define ratio of image to be return by library
         *
         * @param ratio [String] Image ratio in string format e.g. "1:1"
         */
        fun setImageRatio(ratio: String): ActivityBuilder {
            this.ratio = ratio
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
                ERASER_ACTIVITY_REQUEST_CODE
            )
        }
    }
}
