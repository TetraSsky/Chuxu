package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Html
import android.view.Gravity
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
import androidx.core.content.ContextCompat
import com.example.chuxu.R
import java.util.regex.Pattern
import com.example.chuxu.controller.UserController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activité principale de l'application, gère la l'inscription de l'utilisateur
 */
class Activity3 : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVerifEditText: EditText
    private lateinit var nicknameEditText: EditText
    private val inappropriateNicknames = listOf(
        "negro", "négro", "nègre", "BlancSuprémaciste", "Raciste", "KKK", "Esclavage", "Aryan", "Nazi", "Hitler", "Fasciste", "Colonialiste", "Ségrégation", "SuprématieBlanche",
        "Violence", "Haine", "Terroriste", "Pornographie", "Porno", "Automutilation", "Suicide",
        "Trump", "Biden", "Macron", "Poutine", "XiJinping", "Erdogan", "KimJongUn", "Assad", "Netanyahu", "Merkel", "Laden", "Al-Qaïda",
        "WhiteSupremacist", "Racist", "Slavery", "Aryan", "Nazi", "Hitler", "Fascist", "Colonialist", "Segregation", "WhiteSupremacy",
        "Violence", "Hate", "Terrorist", "Pornography", "SelfHarm", "Sex", "Fuck",
        "BlancoSupremacista", "Racista", "Esclavitud", "Nazi", "Hitler", "Fascista", "Colonialista", "Segregación",
        "Terrorista", "Pornografía", "Automutilación", "Suicidio",
    )
    private var isShowingLoadingView = false
    private lateinit var loadingView: View
    private lateinit var loadingTextView1: TextView
    private lateinit var loadingTextView2: TextView
    private var passwordInfoPopup: PopupWindow? = null
    private var passwordVerifInfoPopup: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inscription)

        enableEdgeToEdge()

        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.Password)
        passwordVerifEditText = findViewById(R.id.PasswordVerif)
        nicknameEditText = findViewById(R.id.Nickname)
        val inscripButton = findViewById<Button>(R.id.Inscrip)
        val passwordInfo = findViewById<ImageView>(R.id.passwordInfo)
        val passwordVerifInfo = findViewById<ImageView>(R.id.passwordVerifInfo)

        passwordInfo.setOnClickListener {
            showInfoPopup(passwordInfo, "Le mot de passe doit contenir au moins 8 caractères, un chiffre, une majuscule, une minuscule et un caractère spécial")
        }

        passwordVerifInfo.setOnClickListener {
            showInfoPopup(passwordVerifInfo, "Les mots de passe doivent être les mêmes !")
        }

        inscripButton.isEnabled = true
        inscripButton.setOnClickListener {
            inscripButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString()
                val passwordVerif = passwordVerifEditText.text.toString()
                val nickname = nicknameEditText.text.toString().trim()

                if (email.isEmpty() || password.isEmpty() || passwordVerif.isEmpty() || nickname.isEmpty()) {
                    Toast.makeText(this@Activity3, "Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    inscripButton.isEnabled = true
                } else if (!isEmailValid(email)) {
                    Toast.makeText(this@Activity3, "L'email en entrée est invalide !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    inscripButton.isEnabled = true
                } else if (!isPasswordValid(password)) {
                    Toast.makeText(this@Activity3, "Mot de passe invalide ou trop peu sécurisé.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    inscripButton.isEnabled = true
                } else if (password != passwordVerif) {
                    Toast.makeText(this@Activity3, "Les mots de passe ne correspondent pas.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    inscripButton.isEnabled = true
                } else if (!isNicknameValid(nickname)) {
                    Toast.makeText(this@Activity3, "Pseudo invalide.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    inscripButton.isEnabled = true
                } else if (nickname.length < 30) {
                    Toast.makeText(this@Activity3, "Les pseudos sont limités à 30 caractères !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    inscripButton.isEnabled = true
                } else {
                    showLoadingView("Inscription...", "Veuillez patienter...")
                    val encryptedPassword = UserController.encryptPassword(password)
                    RegisterUserTask().execute(email, encryptedPassword, nickname)
                }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Pattern.compile("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$")
        return emailPattern.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@!?_%$|\\\\]).{8,}$")
        return passwordPattern.matcher(password).matches()
    }

    private fun isNicknameValid(nickname: String): Boolean {
        val nicknamePattern = Pattern.compile("^[a-zA-Z0-9!?@_-]+$")
        val isNicknameValid = nicknamePattern.matcher(nickname).matches()

        val containsInappropriate = inappropriateNicknames.any { inappropriateNickname ->
            nickname.contains(inappropriateNickname, ignoreCase = true)
        }

        val isNumeric = nickname.matches("[0-9]+".toRegex())
        val isLengthValid = nickname.length >= 5

        return isNicknameValid && !containsInappropriate && !isNumeric && isLengthValid
    }

    inner class RegisterUserTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String): Boolean {
            val email = params[0]
            val userPassword = params[1]
            val nickname = params[2]

            return UserController.registerUser(email, userPassword, nickname)
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                CoroutineScope(Dispatchers.Main).launch {
                    val email = emailEditText.text.toString().trim()
                    val userID = withContext(Dispatchers.IO) {
                        UserController.getUserID(email)
                    }

                    if (userID != 0) {
                        hideLoadingView()

                        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", true)
                        editor.putString("userEmail", email)
                        editor.putString("userNickname", nicknameEditText.text.toString().trim())
                        editor.putInt("userID", userID)
                        editor.apply()

                        val intent = Intent(this@Activity3, Activity2::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        hideLoadingView()
                        Toast.makeText(this@Activity3, "Une erreur est survenue.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                hideLoadingView()

                Toast.makeText(applicationContext, "Erreur lors de l'inscription", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showInfoPopup(anchorView: ImageView, message: String) {
        val textView = TextView(this)
        textView.text = Html.fromHtml("<p>$message</p>")
        textView.textSize = 14f

        val width = resources.getDimensionPixelSize(R.dimen.popup_width)
        val height = resources.getDimensionPixelSize(R.dimen.popup_height)
        val popupView = PopupWindow(textView, width, height, true)
        popupView.contentView = textView
        popupView.showAsDropDown(anchorView, 0, 0, Gravity.END)
    }

    override fun onDestroy() {
        passwordInfoPopup?.dismiss()
        passwordVerifInfoPopup?.dismiss()
        super.onDestroy()
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