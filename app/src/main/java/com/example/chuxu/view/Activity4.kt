package com.example.chuxu.view

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.example.chuxu.controller.UserController
import com.example.chuxu.util.UpdateUtil
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Activité secondaire de l'application, permet à l'utilisateur de gérer les informations de son compte
 */
class Activity4 : AppCompatActivity() {

    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVerifEditText : EditText
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
    private lateinit var NouvEmailButton: Button
    private lateinit var NouvMDPButton: Button
    private lateinit var NouvNicknameButton: Button
    private lateinit var DeleteAccountButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var popupTextView1: TextView
    private lateinit var popupTextView2: TextView
    private lateinit var passwordInfo: ImageView
    private lateinit var passwordVerifInfo: ImageView
    private lateinit var NicknameInfo: ImageView
    private lateinit var passwordInfoPopup: PopupWindow
    private lateinit var passwordVerifInfoPopup: PopupWindow
    private lateinit var nicknameInfoPopup: PopupWindow
    private var isPopupShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compteinfo)

        enableEdgeToEdge()

        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.Password)
        passwordVerifEditText = findViewById(R.id.PasswordVerif)
        nicknameEditText = findViewById(R.id.Nickname)
        NouvEmailButton = findViewById(R.id.NouvEmail)
        NouvMDPButton = findViewById(R.id.NouvMDP)
        NouvNicknameButton = findViewById(R.id.NouvNickname)
        DeleteAccountButton = findViewById(R.id.DeleteAccount)
        drawerLayout = findViewById(R.id.MenuBurger)
        navView = findViewById(R.id.nav_view)
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        val userID = sharedPref.getInt("userID", 0)
        passwordInfo = findViewById(R.id.passwordInfo)
        passwordVerifInfo = findViewById(R.id.passwordVerifInfo)
        NicknameInfo = findViewById(R.id.NicknameInfo)
        passwordInfoPopup = PopupWindow(this)
        passwordVerifInfoPopup = PopupWindow(this)
        nicknameInfoPopup = PopupWindow(this)

        passwordInfo.setOnClickListener {
            showInfoPopup(passwordInfo, passwordInfoPopup, "Le mot de passe doit contenir au moins :", "- 8 caractères\n- Un chiffre\n- Une majuscule\n- Une minuscule\n- Un caractère spécial")
        }

        passwordVerifInfo.setOnClickListener {
            showInfoPopup(passwordVerifInfo, passwordVerifInfoPopup, "Les mots de passe", "doivent correspondre !")
        }

        NicknameInfo.setOnClickListener {
            showInfoPopup(NicknameInfo, nicknameInfoPopup, "Le pseudonyme doit :", "- Contenir au moins 5 caractères.\n- Ne peut pas être que des chiffres.\n- Ne pas contenir de termes bannis/sensibles.\n- Pas plus long que 30 caractères.\n- Etre en alphanumérique uniquement.")
        }

        NouvEmailButton.isEnabled = true
        NouvEmailButton.setOnClickListener {
            NouvEmailButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val email = emailEditText.text.toString().trim()

                if (email.isEmpty()) {
                    Toast.makeText(this@Activity4, "Veuillez remplir le champ !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    NouvEmailButton.isEnabled = true
                } else if (!isEmailValid(email)) {
                    Toast.makeText(this@Activity4, "L'email en entrée est invalide !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    NouvEmailButton.isEnabled = true
                } else {

                    val nouvEmail = emailEditText.text.toString().trim()
                    val success = UserController.newUserEmail(nouvEmail, userID)

                    if (success) {
                        DatabaseManager.closeConnection()

                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", false)
                        editor.apply()

                        Toast.makeText(this@Activity4, "Email modifié avec succès !", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@Activity4, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Activity4, "Échec de la modification de l'email.", Toast.LENGTH_LONG).show()
                        delay(4000)
                        NouvEmailButton.isEnabled = true
                    }
                }
            }
        }

        NouvMDPButton.isEnabled = true
        NouvMDPButton.setOnClickListener {
            NouvMDPButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val password = passwordEditText.text.toString().trim()
                val passwordVerif = passwordVerifEditText.text.toString().trim()

                if (password.isEmpty() || passwordVerif.isEmpty()) {
                    Toast.makeText(this@Activity4, "Veuillez remplir les champs !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    NouvMDPButton.isEnabled = true
                } else if (!isPasswordValid(password)) {
                    Toast.makeText(this@Activity4, "Mot de passe invalide ou trop peu sécurisé.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    NouvMDPButton.isEnabled = true
                } else if (password != passwordVerif) {
                    Toast.makeText(this@Activity4, "Les mots de passe ne correspondent pas.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    NouvMDPButton.isEnabled = true
                } else {

                    val nouvPassword = UserController.encryptPassword(password)
                    val success = UserController.newUserPassword(nouvPassword, userID)

                    if (success) {
                        DatabaseManager.closeConnection()

                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", false)
                        editor.apply()

                        Toast.makeText(this@Activity4, "Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@Activity4, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Activity4, "Échec de la modification du mot de passe.", Toast.LENGTH_LONG).show()
                        delay(4000)
                        NouvMDPButton.isEnabled = true
                    }
                }
            }
        }

        NouvNicknameButton.isEnabled = true
        NouvNicknameButton.setOnClickListener {
            NouvNicknameButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val nickname = nicknameEditText.text.toString().trim()

                if (nickname.isEmpty()) {
                    Toast.makeText(this@Activity4, "Veuillez remplir le champ !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    NouvNicknameButton.isEnabled = true
                } else if (!isNicknameValid(nickname)) {
                    Toast.makeText(this@Activity4, "Pseudo invalide.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    NouvNicknameButton.isEnabled = true
                } else if (nickname.length < 30) {
                    Toast.makeText(this@Activity4, "Les pseudos sont limités à 30 caractères !", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    NouvNicknameButton.isEnabled = true
                } else {

                    val nouvNickname = nicknameEditText.text.toString().trim()
                    val success = UserController.newUserNickname(nouvNickname, userID)

                    if (success) {
                        val nickname = UserController.getUserNickname(userID)
                        if (nickname.isNotEmpty()) {
                            val editor = sharedPref.edit()
                            editor.putString("userNickname", nickname)
                            editor.apply()

                            Toast.makeText(this@Activity4, "Pseudo modifié avec succès !", Toast.LENGTH_LONG).show()

                            val intent = Intent(this@Activity4, Activity2::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Activity4, "Veuillez vous reconnecter.", Toast.LENGTH_SHORT).show()
                            delay(4000)
                            NouvNicknameButton.isEnabled = true
                        }
                    } else {
                        Toast.makeText(this@Activity4, "Échec de la modification du pseudo.", Toast.LENGTH_LONG).show()
                        delay(4000)
                        NouvNicknameButton.isEnabled = true
                    }
                }
            }
        }

        DeleteAccountButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            alertDialogBuilder.apply {
                setTitle("Confirmation")
                setMessage("Êtes-vous sûr de vouloir supprimer votre compte ?")
                setPositiveButton("Oui") { _, _ ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val success = UserController.deleteUserAccount(userID)
                        if (success) {
                            DatabaseManager.closeConnection()

                            val editor = sharedPref.edit()
                            editor.putBoolean("isUserLoggedIn", false)
                            editor.apply()

                            Toast.makeText(this@Activity4, "Votre compte a bien été effacé !", Toast.LENGTH_LONG).show()

                            val intent = Intent(this@Activity4, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Activity4, "Une erreur est survenue.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                setNegativeButton("Non") { _, _ -> }
            }
            val alertDialog = alertDialogBuilder.create()

            alertDialog.show()

            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.red))
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Menu -> {
                    val intent = Intent(this, Activity2::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.Compte -> {
                }
                R.id.Avis -> {
                    val intent = Intent(this, Activity8::class.java)
                    startActivity(intent)
                }
                R.id.Deconnect -> {
                    DatabaseManager.closeConnection()

                    val editor = sharedPref.edit()
                    editor.putBoolean("isUserLoggedIn", false)
                    editor.apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.Update -> {
                    UpdateUtil.checkForUpdate(this)
                }
            }
            true
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
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
}