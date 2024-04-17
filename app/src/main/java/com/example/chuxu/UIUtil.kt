package com.example.chuxu

import android.view.View
import androidx.appcompat.app.AppCompatActivity

object UIUtil {
    fun toggleSystemUI(activity: AppCompatActivity) {
        activity.window.decorView.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }
}