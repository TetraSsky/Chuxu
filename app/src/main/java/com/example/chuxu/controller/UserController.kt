package com.example.chuxu.controller

import com.example.chuxu.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.SQLException
import kotlin.experimental.and
import kotlinx.coroutines.*

/**
 * Contrôleur pour gérer les opérations liées aux utilisateurs.
 */
object UserController {

    /**
     * Connecte un utilisateur avec son email et mot de passe.
     *
     * @param email L'adresse email de l'utilisateur.
     * @param password Le mot de passe de l'utilisateur.
     * @return Une paire indiquant si l'utilisateur est connecté avec succès et son pseudo associé.
     */
    suspend fun loginUser(email: String, password: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
                    val query = "SELECT Password, Nickname FROM Utilisateur WHERE Email=?"
                    val statement = connection.prepareStatement(query)
                    statement.setString(1, email)
                    val resultSet = statement.executeQuery()

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

    /**
     * Crypte un mot de passe en utilisant l'algorithme SHA-256.
     *
     * @param password Le mot de passe à crypter.
     * @return Le mot de passe crypté.
     */
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

    /**
     * Récupère l'ID de l'utilisateur associé à l'adresse email donnée.
     *
     * @param email L'adresse email de l'utilisateur.
     * @return L'ID de l'utilisateur.
     */
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

    /**
     * Récupère le pseudo de l'utilisateur associé à l'ID donné.
     *
     * @param userID L'ID de l'utilisateur.
     * @return Le pseudo de l'utilisateur.
     */
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

    /**
     * Modifie l'adresse email de l'utilisateur.
     *
     * @param email La nouvelle adresse email.
     * @param userID L'ID de l'utilisateur.
     * @return [true] si l'opération a réussi, [false] sinon.
     */
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

    /**
     * Modifie le mot de passe de l'utilisateur.
     *
     * @param password Le nouveau mot de passe.
     * @param userID L'ID de l'utilisateur.
     * @return [true] si l'opération a réussi, [false] sinon.
     */
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

    /**
     * Modifie le pseudo de l'utilisateur.
     *
     * @param nickname Le nouveau pseudo.
     * @param userID L'ID de l'utilisateur.
     * @return [true] si l'opération a réussi, [false] sinon.
     */
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

    /**
     * Enregistre un nouvel utilisateur dans la base de données.
     *
     * @param email L'adresse email du nouvel utilisateur.
     * @param password Le mot de passe du nouvel utilisateur.
     * @param nickname Le pseudo du nouvel utilisateur.
     * @return [true] si l'enregistrement a réussi, [false] sinon.
     */
    fun registerUser(email: String, password: String, nickname: String): Boolean {
        var connection = DatabaseManager.getConnection()
        var isEmailAvailable = false
        var isNicknameAvailable = false

        try {
            if (connection != null) {
                val emailCheckQuery = "SELECT COUNT(*) FROM Utilisateur WHERE Email = ?"
                val emailCheckStatement = connection.prepareStatement(emailCheckQuery)
                emailCheckStatement.setString(1, email)
                val emailCheckResult = emailCheckStatement.executeQuery()
                if (emailCheckResult.next()) {
                    val count = emailCheckResult.getInt(1)
                    isEmailAvailable = count == 0
                }
                emailCheckStatement.close()

                val nicknameCheckQuery = "SELECT COUNT(*) FROM Utilisateur WHERE Nickname = ?"
                val nicknameCheckStatement = connection.prepareStatement(nicknameCheckQuery)
                nicknameCheckStatement.setString(1, nickname)
                val nicknameCheckResult = nicknameCheckStatement.executeQuery()
                if (nicknameCheckResult.next()) {
                    val count = nicknameCheckResult.getInt(1)
                    isNicknameAvailable = count == 0
                }
                nicknameCheckStatement.close()

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

    /**
     * Supprime définitivement le compte d'un utilisateur.
     *
     * @param userID L'ID de l'utilisateur à supprimer.
     * @return [true] si la suppression a réussi, [false] sinon.
     */
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

    /**
     * Insère une nouvelle review dans la base de données.
     *
     * @param userId L'ID de l'utilisateur laissant la review.
     * @param gameId L'ID du jeu concerné par la review.
     * @param review Le contenu de la review.
     * @return [true] si l'insertion a réussi, [false] sinon.
     */
    suspend fun createReview(userID: Int, gameID: Int, message: String): Boolean {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
                    val checkExistingReviewQuery = "SELECT COUNT(*) FROM Avis WHERE UtilisateurID = ? AND GameID = ?"
                    val checkExistingReviewStatement = connection.prepareStatement(checkExistingReviewQuery)
                    checkExistingReviewStatement.setInt(1, userID)
                    checkExistingReviewStatement.setInt(2, gameID)
                    val existingReviewResult = checkExistingReviewStatement.executeQuery()
                    existingReviewResult.next()
                    val existingReviewCount = existingReviewResult.getInt(1)
                    checkExistingReviewStatement.close()

                    if (existingReviewCount == 0) {
                        val insertReviewQuery = "INSERT INTO Avis (UtilisateurID, GameID, Message) VALUES (?, ?, ?)"
                        val insertReviewStatement = connection.prepareStatement(insertReviewQuery)
                        insertReviewStatement.setInt(1, userID)
                        insertReviewStatement.setInt(2, gameID)
                        insertReviewStatement.setString(3, message)
                        val isReviewInserted = insertReviewStatement.executeUpdate()
                        insertReviewStatement.close()
                        return@withContext isReviewInserted > 0
                    } else {
                        val updateReviewQuery = "UPDATE Avis SET Message = ? WHERE UtilisateurID = ? AND GameID = ?"
                        val updateReviewStatement = connection.prepareStatement(updateReviewQuery)
                        updateReviewStatement.setString(1, message)
                        updateReviewStatement.setInt(2, userID)
                        updateReviewStatement.setInt(3, gameID)
                        val isReviewUpdated = updateReviewStatement.executeUpdate()
                        updateReviewStatement.close()
                        return@withContext isReviewUpdated > 0
                    }
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