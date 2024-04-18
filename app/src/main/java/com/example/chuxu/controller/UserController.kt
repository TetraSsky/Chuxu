package com.example.chuxu.controller

import com.example.chuxu.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.SQLException
import kotlin.experimental.and
import kotlinx.coroutines.*

object UserController {

    // Fonction pour connecter l'utilisateur
    suspend fun loginUser(email: String, password: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
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
    fun encryptPassword(password: String): String {
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

    // Fonction pour récupérer l'ID de l'utilisateur connecté
    suspend fun getUserID(email: String): Int {
        return withContext(Dispatchers.IO) {
            var userID = 0
            try {
                val connection = DatabaseManager.getConnection()
                if (connection != null) {
                    val query = "SELECT UtilisateurID FROM Utilisateur WHERE Email=?"
                    val statement = connection.prepareStatement(query)
                    statement.setString(1, email)
                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        userID = resultSet.getInt("UtilisateurID")
                    }
                    statement.close()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            userID
        }
    }

    //Fonction pour récupérer de nickname de l'utilisateur connecté
    suspend fun getUserNickname(userID: Int): String {
        return withContext(Dispatchers.IO) {
            var userNickname = ""
            try {
                val connection = DatabaseManager.getConnection()
                if (connection != null) {
                    val query = "SELECT Nickname FROM Utilisateur WHERE UtilisateurID=?"
                    val statement = connection.prepareStatement(query)
                    statement.setInt(1, userID)
                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        userNickname = resultSet.getString("Nickname")
                    }
                    statement.close()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            userNickname
        }
    }

    // Fonction pour modifier l'e-mail de l'utilisateur
    suspend fun newUserEmail(email: String, userID: Int): Boolean {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
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
                        return@withContext isEmailAvailable
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            false
        }
    }

    // Fonction pour modifier le mot de passe
    suspend fun newUserPassword(password: String, userID: Int): Boolean {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
                    val updatePasswordQuery = "UPDATE Utilisateur SET Password = ? WHERE UtilisateurID = ?"
                    val updatePasswordStatement = connection.prepareStatement(updatePasswordQuery)
                    updatePasswordStatement.setString(1, password)
                    updatePasswordStatement.setInt(2, userID)
                    val isPasswordChanged = updatePasswordStatement.executeUpdate()
                    updatePasswordStatement.close()

                    if (isPasswordChanged != 0) {
                        return@withContext true
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            false
        }
    }

    // Fonction pour modifier le pseudo
    suspend fun newUserNickname(nickname: String, userID: Int): Boolean {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
                    val updateNicknameQuery = "UPDATE Utilisateur SET Nickname = ? WHERE UtilisateurID = ?"
                    val updateNicknameStatement = connection.prepareStatement(updateNicknameQuery)
                    updateNicknameStatement.setString(1, nickname)
                    updateNicknameStatement.setInt(2, userID)
                    val isNicknameChanged = updateNicknameStatement.executeUpdate()
                    updateNicknameStatement.close()

                    if (isNicknameChanged != 0) {
                        return@withContext true
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            false
        }
    }

    // Fonction pour enregistrer un nouvel utilisateur || Inscription
    fun registerUser(email: String, password: String, nickname: String): Boolean {
        var connection = DatabaseManager.getConnection()
        var isEmailAvailable = false
        var isNicknameAvailable = false

        try {
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
                    val query = "INSERT INTO Utilisateur (Email, Password, Nickname) VALUES ('$email', '$password', '$nickname')"
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

    // Fonction pour supprimer un utilisateur (Définitivement, y compris ses avis)
    suspend fun deleteUserAccount(userID: Int): Boolean {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
                    val deleteAccountQuery = "DELETE FROM Utilisateur WHERE UtilisateurID = ?"
                    val deleteAccountStatement = connection.prepareStatement(deleteAccountQuery)
                    deleteAccountStatement.setInt(1, userID)
                    val isAccountDeleted = deleteAccountStatement.executeUpdate()
                    if (isAccountDeleted > 0) {
                        return@withContext true
                    }
                    deleteAccountStatement.close()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                connection?.close()
            }
            false
        }
    }
}