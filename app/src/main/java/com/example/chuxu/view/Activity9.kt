package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chuxu.R
import com.example.chuxu.util.EmailUtil

/**
 * Activité principale de l'application, permet à l'utilisateur d'effectuer une demande de réinitialisation de mot de passe
 */
class Activity9 : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var codeEditText: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var confirmButton: Button
    private var generatedCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mdpoublie)

        enableEdgeToEdge()

        emailEditText = findViewById(R.id.EmailVerif)
        codeEditText = findViewById(R.id.Code)
        sendCodeButton = findViewById(R.id.SendCode)
        confirmButton = findViewById(R.id.Confirm)

        confirmButton.isEnabled = false
        sendCodeButton.isEnabled = true
        sendCodeButton.setOnClickListener {
            sendCodeButton.isEnabled = false
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                generatedCode = generateVerificationCode()
                EmailUtil.sendEmail(email, "Code de vérification", "Votre code de vérification est : $generatedCode")
                Toast.makeText(this, "Code envoyé à $email", Toast.LENGTH_SHORT).show()
                confirmButton.isEnabled = true
            } else {
                sendCodeButton.isEnabled = true
                Toast.makeText(this, "Veuillez entrer une adresse email", Toast.LENGTH_SHORT).show()
            }
        }

        confirmButton.setOnClickListener {
            confirmButton.isEnabled = false
            val inputCode = codeEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            if (inputCode.isNotEmpty()) {
                if (inputCode == generatedCode) {
                    val intent = Intent(this, Activity10::class.java)
                    val sharedPref = getSharedPreferences("NEW_PASSWORD_MY_APP_PREF", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("userEmail", email)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Code incorrect", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Veuillez entrer le code de vérification", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }
}