package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import com.example.chuxu.controller.UserController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Activité tierciaire de l'application, permet à l'utilisateur de réinitialiser son mot de passe
 */
class Activity10 : AppCompatActivity() {
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordButton: Button
    private lateinit var popupTextView1: TextView
    private lateinit var popupTextView2: TextView
    private lateinit var passwordInfo: ImageView
    private lateinit var passwordInfoPopup: PopupWindow
    private var isPopupShowing = false
    private var isShowingLoadingView = false
    private lateinit var loadingView: View
    private lateinit var loadingTextView1: TextView
    private lateinit var loadingTextView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newmdp)

        enableEdgeToEdge()

        newPasswordEditText = findViewById(R.id.newPassword)
        confirmNewPasswordButton = findViewById(R.id.confirmNewPassword)
        passwordInfo = findViewById(R.id.passwordInfo)
        passwordInfoPopup = PopupWindow(this)
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)

        passwordInfo.setOnClickListener {
            showInfoPopup(passwordInfo, passwordInfoPopup, "Le mot de passe doit contenir au moins :", "- 8 caractères.\n- Un chiffre.\n- Une majuscule.\n- Une minuscule.\n- Un caractère spécial.")
        }

        confirmNewPasswordButton.isEnabled = true
        confirmNewPasswordButton.setOnClickListener {
            confirmNewPasswordButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val newPassword = newPasswordEditText.text.toString().trim()
                showLoadingView("Changement du mot de passe...", "Veuillez patienter...")
                if (newPassword.isEmpty()) {

                    hideLoadingView()

                    Toast.makeText(this@Activity10, "Veuillez remplir les champs !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    confirmNewPasswordButton.isEnabled = true
                } else if (!isPasswordValid(newPassword)) {

                    hideLoadingView()

                    Toast.makeText(this@Activity10, "Mot de passe invalide ou trop peu sécurisé.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    confirmNewPasswordButton.isEnabled = true
                } else {
                    val nouvPassword = UserController.encryptPassword(newPassword)

                    val email = sharedPref.getString("userEmail", "")
                    val userID = email?.let { it1 -> UserController.getUserID(it1) }

                    if (userID != 0) {
                        val success = userID?.let { it1 -> UserController.newUserPassword(nouvPassword, it1) }
                        if (success == true) {

                            hideLoadingView()

                            Toast.makeText(this@Activity10, "Mot de passe modifié avec succès !\nEssayez de vous reconnecter.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@Activity10, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {

                            hideLoadingView()

                            Toast.makeText(this@Activity10, "Échec de la modification du mot de passe.", Toast.LENGTH_LONG).show()
                            delay(4000)
                            confirmNewPasswordButton.isEnabled = true
                        }
                    } else {

                        hideLoadingView()

                        Toast.makeText(this@Activity10, "Une erreur est survenue.", Toast.LENGTH_LONG).show()
                        delay(4000)
                        confirmNewPasswordButton.isEnabled = true
                    }
                }
            }
        }
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

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@!?_%$|\\\\]).{8,}$")
        return passwordPattern.matcher(password).matches()
    }

    private fun showLoadingView(text1: String, text2: String) {
        if (!isShowingLoadingView) {
            isShowingLoadingView = true
            loadingView = layoutInflater.inflate(R.layout.loading_screen, findViewById(android.R.id.content), false)
            loadingTextView1 = loadingView.findViewById(R.id.loadingtextView1)
            loadingTextView2 = loadingView.findViewById(R.id.loadingtextView2)
            loadingTextView1.text = text1
            loadingTextView2.text = text2
            (findViewById<ViewGroup>(android.R.id.content)).addView(loadingView)
        } else {
            updateLoadingView(text1, text2)
        }
    }

    private fun hideLoadingView() {
        isShowingLoadingView = false
        (findViewById<ViewGroup>(android.R.id.content)).removeView(loadingView)
    }

    private fun updateLoadingView(text1: String, text2: String) {
        loadingTextView1.text = text1
        loadingTextView2.text = text2
    }
}