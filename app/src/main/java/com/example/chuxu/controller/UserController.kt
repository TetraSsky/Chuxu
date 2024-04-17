package com.example.chuxu.controller

import com.example.chuxu.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.Connection
import java.sql.SQLException
import kotlin.experimental.and

object UserController {

    // Fonction pour connecter l'utilisateur
    suspend fun loginUser(email: String, password: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
                    // Requête pour récupérer le mot de passe crypté de l'utilisateur
                    val query = "SELECT Password, Nickname FROM Utilisateur WHERE Email=?"
                    val statement = connection.prepareStatement(query)
                    statement.setString(1, email)
                    val resultSet = statement.executeQuery()

                    // Vérification si l'utilisateur existe dans la base de données avant de vérifier le mot de passe crypté
                    if (resultSet.next()) {
                        val storedPassword = resultSet.getString("Password")
                        val nickname = resultSet.getString("Nickname")
                        val passwordMatch = storedPassword == encryptPassword(password)
                        return@withContext Pair(passwordMatch, nickname)
                    } else {
                        return@withContext Pair(false, "")
                    }
                } else {
                    return@withContext Pair(false, "")
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                return@withContext Pair(false, "")
            }
        }
    }

    // Fonction pour crypter le mot de passe
    private fun encryptPassword(password: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
            md.update(password.toByteArray())

            val byteData = md.digest()
            val sb = StringBuilder()
            for (i in byteData.indices) {
                sb.append(
                    ((byteData[i] and 0xff.toByte()) + 0x100).toString(16).substring(1)
                )
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    // Fonction pour enregistrer un nouvel utilisateur || Inscription
    fun registerUser(email: String, password: String, nickname: String): Boolean {
        var connection = DatabaseManager.getConnection()
        var isEmailAvailable = false
        var isNicknameAvailable = false

        try {
            // Récupérer la connexion à partir de DatabaseManager
            connection = DatabaseManager.getConnection()

            // Vérifier si la connexion est réussie
            if (connection != null) {
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
                    val query =
                        "INSERT INTO Utilisateur (Email, Password, Nickname) VALUES ('$email', '$password', '$nickname')"
                    statement.executeUpdate(query)
                    statement.close()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
        return isEmailAvailable && isNicknameAvailable
    }
}