package com.example.chuxu.controller

import com.example.chuxu.DatabaseManager
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.Connection
import java.sql.SQLException
import kotlin.experimental.and

object UserController {

    private lateinit var connection: Connection

    // Fonction pour initialiser la connexion
    fun initializeConnection() {
        connection = DatabaseManager.getConnection() ?: throw SQLException("Connection not initialized")
    }

    // Fonction pour connecter l'utilisateur
    fun loginUser(email: String, password: String): Pair<Boolean, String> {
        try {
            val query = "SELECT Password, Nickname, UtilisateurID FROM Utilisateur WHERE Email=?"
            val statement = connection.prepareStatement(query)
            statement.setString(1, email)
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                val storedPassword = resultSet.getString("Password")
                val nickname = resultSet.getString("Nickname")
                val passwordMatch = storedPassword == encryptPassword(password)
                return Pair(passwordMatch, nickname)
            } else {
                return Pair(false, "")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            return Pair(false, "")
        }
    }

    // Fonction pour crypter le mot de passe
    private fun encryptPassword(password: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(password.toByteArray())

            val byteData = md.digest()
            val sb = StringBuilder()
            for (i in byteData.indices) {
                sb.append(((byteData[i] and 0xff.toByte()) + 0x100).toString(16).substring(1))
            }
            sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            ""
        }
    }

    // Fonction pour récupérer l'ID de l'utilisateur connecté
    fun getUserID(email: String): Int? {
        var userID: Int? = null
        try {
            val query = "SELECT UtilisateurID FROM Utilisateur WHERE Email=?"
            val statement = connection.prepareStatement(query)
            statement.setString(1, email)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                userID = resultSet.getInt("UtilisateurID")
            }
            statement.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return userID
    }

    // Fonction pour enregistrer un nouvel utilisateur || Inscription
    fun registerUser(email: String, password: String, nickname: String): Boolean {
        var isEmailAvailable = false
        var isNicknameAvailable = false

        try {
            // Vérifier si l'email est disponible
            val emailCheckQuery = "SELECT COUNT(*) FROM Utilisateur WHERE Email = ?"
            val emailCheckStatement = connection.prepareStatement(emailCheckQuery)
            emailCheckStatement.setString(1, email)
            val emailCheckResult = emailCheckStatement.executeQuery()
            if (emailCheckResult.next()) {
                val count = emailCheckResult.getInt(1)
                isEmailAvailable = count == 0
            }
            emailCheckStatement.close()

            // Vérifier si le pseudo est disponible
            val nicknameCheckQuery = "SELECT COUNT(*) FROM Utilisateur WHERE Nickname = ?"
            val nicknameCheckStatement = connection.prepareStatement(nicknameCheckQuery)
            nicknameCheckStatement.setString(1, nickname)
            val nicknameCheckResult = nicknameCheckStatement.executeQuery()
            if (nicknameCheckResult.next()) {
                val count = nicknameCheckResult.getInt(1)
                isNicknameAvailable = count == 0
            }
            nicknameCheckStatement.close()

            // Si l'email et le pseudo sont disponibles, enregistrer l'utilisateur dans la base de données
            if (isEmailAvailable && isNicknameAvailable) {
                val statement = connection.createStatement()
                val query = "INSERT INTO Utilisateur (Email, Password, Nickname) VALUES ('$email', '$password', '$nickname')"
                statement.executeUpdate(query)
                statement.close()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return isEmailAvailable && isNicknameAvailable
    } // Ne pas fermer la connexion exprès

    // Fonction pour modifier l'e-mail de l'utilisateur
    fun newUserEmail(userID: Int, email: String): Boolean {
        try {
            val emailCheckQuery = "SELECT COUNT(*) FROM Utilisateur WHERE Email = ? AND UtilisateurID != ?"
            val emailCheckStatement = connection.prepareStatement(emailCheckQuery)
            emailCheckStatement.setString(1, email)
            emailCheckStatement.setInt(2, userID)
            val emailCheckResult = emailCheckStatement.executeQuery()

            if (emailCheckResult.next()) {
                val count = emailCheckResult.getInt(1)
                val isEmailAvailable = count == 0

                if (isEmailAvailable) {
                    val updateEmailQuery = "UPDATE Utilisateur SET Email = ? WHERE UtilisateurID = ?"
                    val updateEmailStatement = connection.prepareStatement(updateEmailQuery)
                    updateEmailStatement.setString(1, email)
                    updateEmailStatement.setInt(2, userID)
                    updateEmailStatement.executeUpdate()
                    updateEmailStatement.close()
                }
                return isEmailAvailable
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    /*
        fun newUserPassword(password: String): Boolean{

        }

        fun newUserNickname(nickname: String): Boolean{

        }*/
}