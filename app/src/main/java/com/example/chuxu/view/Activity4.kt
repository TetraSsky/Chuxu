package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.example.chuxu.controller.UserController
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compteinfo)

        enableEdgeToEdge()

        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.Password)
        passwordVerifEditText = findViewById(R.id.PasswordVerif)
        nicknameEditText = findViewById(R.id.Nickname)
        val NouvEmailButton = findViewById<Button>(R.id.NouvEmail)
        val NouvMDPButton = findViewById<Button>(R.id.NouvMDP)
        val NouvNicknameButton = findViewById<Button>(R.id.NouvNickname)
        val DeleteAccountButton = findViewById<Button>(R.id.DeleteAccount)
        val drawerLayout : DrawerLayout = findViewById(R.id.MenuBurger)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        val userID = sharedPref.getInt("userID", 0)

        NouvEmailButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir le champ !", Toast.LENGTH_SHORT).show()
            } else if (!isEmailValid(email)) {
                Toast.makeText(this, "L'email en entrée est invalide !", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
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
                        Toast.makeText(this@Activity4,"Échec de la modification de l'email.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        NouvMDPButton.setOnClickListener {
            val password = passwordEditText.text.toString()
            val passwordVerif = passwordVerifEditText.text.toString()

            if (password.isEmpty() || passwordVerif.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir les champs !", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid(password)) {
                Toast.makeText(this, "Mot de passe invalide ou trop peu sécurisé.", Toast.LENGTH_SHORT).show()
            } else if (password != passwordVerif) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val nouvPassword = UserController.encryptPassword(password)
                    val success = UserController.newUserPassword(nouvPassword, userID)

                    if (success) {
                        DatabaseManager.closeConnection()

                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", false)
                        editor.apply()

                        Toast.makeText(this@Activity4,"Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@Activity4, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Activity4, "Échec de la modification du mot de passe.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        NouvNicknameButton.setOnClickListener {
            val nickname = nicknameEditText.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir le champ !", Toast.LENGTH_SHORT).show()
            } else if (!isNicknameValid(nickname)) {
                Toast.makeText(this, "Pseudo invalide.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
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
                            Toast.makeText(this@Activity4, "Veuillez vous reconnecter.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@Activity4, "Échec de la modification du pseudo.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        DeleteAccountButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer votre compte ?")
                .setPositiveButton("Oui") { _, _ ->
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
                .setNegativeButton("Non") { _, _ ->
                }
                .show()
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
                R.id.Deconnect -> {
                    DatabaseManager.closeConnection()

                    val editor = sharedPref.edit()
                    editor.putBoolean("isUserLoggedIn", false)
                    editor.apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            true
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