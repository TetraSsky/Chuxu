package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chuxu.R
import com.example.chuxu.util.EmailUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activité principale de l'application, permet à l'utilisateur d'effectuer une demande de réinitialisation de mot de passe
 */
class Activity9 : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var codeEditText: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var confirmButton: Button
    private var generatedCode: String? = null
    private lateinit var popupTextView1: TextView
    private lateinit var popupTextView2: TextView
    private lateinit var codeInfo: ImageView
    private lateinit var codeInfoPopup: PopupWindow
    private var isPopupShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mdpoublie)

        enableEdgeToEdge()

        emailEditText = findViewById(R.id.EmailVerif)
        codeEditText = findViewById(R.id.Code)
        sendCodeButton = findViewById(R.id.SendCode)
        confirmButton = findViewById(R.id.Confirm)
        codeInfo = findViewById(R.id.CodeInfo)
        codeInfoPopup = PopupWindow(this)

        codeInfo.setOnClickListener {
            showInfoPopup(codeInfo, codeInfoPopup, "Pour ne pouvez envoyer un code", "que par intervalle de 120 secondes !")
        }

        confirmButton.isEnabled = false
        sendCodeButton.isEnabled = true
        sendCodeButton.setOnClickListener {
            sendCodeButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val email = emailEditText.text.toString().trim()
                if (email.isNotEmpty()) {
                    generatedCode = generateVerificationCode()
                    EmailUtil.sendEmail(email, "Code de vérification", "Votre code de vérification est : $generatedCode")
                    Toast.makeText(this@Activity9, "Code envoyé à $email", Toast.LENGTH_SHORT).show()
                    delay(120000)
                    sendCodeButton.isEnabled = true
                } else {
                    Toast.makeText(this@Activity9, "Veuillez entrer une adresse email", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    sendCodeButton.isEnabled = true
                }
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

    private fun showInfoPopup(anchorView: ImageView, popupWindow: PopupWindow, text1: String, text2: String) {
        if (!isPopupShowing) {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_window, null)
            popupTextView1 = popupView.findViewById(R.id.text1)
            popupTextView2 = popupView.findViewById(R.id.text2)
            popupTextView1.text = text1
            popupTextView2.text = text2
            popupWindow.contentView = popupView
            popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
            popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
            popupWindow.isOutsideTouchable = true

            popupWindow.showAsDropDown(anchorView)
            isPopupShowing = true
        } else {
            popupWindow.dismiss()
            isPopupShowing = false
        }
    }
}