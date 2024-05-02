package com.example.chuxu.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chuxu.R

/**
 * Activité tierciaire de l'application, permet à l'utilisateur de laisser une review à un jeu
 */
class Activity6 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review)

        enableEdgeToEdge()
    }
}