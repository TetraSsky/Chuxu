package com.example.chuxu.controller

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.chuxu.DatabaseManager
import com.example.chuxu.view.GameReviewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Contrôleur pour gérer les opérations liées aux avis
 */
object ReviewsController {
    
    /**
     * Vérifie si l'utilisateur possède déjà une review sur un jeu précis
     *
     * @param userId L'ID de l'utilisateur laissant la review
     * @param gameId L'ID du jeu concerné par la review
     * @return Le message de sa review si il en possède une pour X jeu, null sinon
     */
    suspend fun getUserReview(userID: Int, gameID: Int): String? {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                connection = DatabaseManager.getConnection()
                if (connection != null) {
                    val query = "SELECT Message FROM Avis WHERE UtilisateurID = ? AND GameID = ?"
                    val statement = connection.prepareStatement(query)
                    statement.setInt(1, userID)
                    statement.setInt(2, gameID)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        return@withContext resultSet.getString("Message")
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                connection?.close()
            }
            return@withContext null
        }
    }

    /**
     * Vérifie si l'utilisateur possède déjà une review sur un jeu précis
     *
     * @param userId L'ID de l'utilisateur auquel appartient la review
     * @param reviewID L'ID de la review à modifier
     * @return Une paire contenant l'ID et le nom du jeu concerné par la review, null si aucune review n'est trouvée
     */
    suspend fun modifyUserReview(userID: Int, reviewID: Int): Pair<Int, String>? {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                connection = DatabaseManager.getConnection()
                if (connection != null) {
                    val query = "SELECT GameID, GameName FROM Avis WHERE UtilisateurID = ? AND AvisID = ?"
                    val statement = connection.prepareStatement(query)
                    statement.setInt(1, userID)
                    statement.setInt(2, reviewID)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val GameID = resultSet.getInt("GameID")
                        val GameName = resultSet.getString("GameName")
                        return@withContext Pair(GameID, GameName)
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                connection?.close()
            }
            return@withContext null
        }
    }

    /**
     * Insère une nouvelle review dans la base de données
     *
     * @param userId L'ID de l'utilisateur laissant la review
     * @param gameId L'ID du jeu concerné par la review
     * @param message Le contenu de la review
     * @param appName le nom du jeu
     * @return [true] si l'insertion a réussi, [false] sinon
     */
    suspend fun createReview(userID: Int, gameID: Int, message: String, appName: String?, time: String): Boolean {
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
                        val insertReviewQuery = "INSERT INTO Avis (UtilisateurID, GameID, Message, GameName, AvisTime) VALUES (?, ?, ?, ?, ?)"
                        val insertReviewStatement = connection.prepareStatement(insertReviewQuery)
                        insertReviewStatement.setInt(1, userID)
                        insertReviewStatement.setInt(2, gameID)
                        insertReviewStatement.setString(3, message)
                        insertReviewStatement.setString(4, appName)
                        insertReviewStatement.setString(5, time)
                        val isReviewInserted = insertReviewStatement.executeUpdate()
                        insertReviewStatement.close()
                        return@withContext isReviewInserted > 0
                    } else {
                        val updateReviewQuery = "UPDATE Avis SET Message = ?, AvisTime = ? WHERE UtilisateurID = ? AND GameID = ?"
                        val updateReviewStatement = connection.prepareStatement(updateReviewQuery)
                        updateReviewStatement.setString(1, message)
                        updateReviewStatement.setString(2, time)
                        updateReviewStatement.setInt(3, userID)
                        updateReviewStatement.setInt(4, gameID)
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

    /**
     * Récupère les avis des utilisateurs pour un jeu spécifique depuis la base de données
     *
     * @param appId L'ID du jeu pour lequel récupérer les avis
     * @return [reviews] Une liste d'objets GameReviewModel représentant les avis des utilisateurs sur le jeu
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchGameReviews(appId: Int): List<GameReviewModel> {
        return withContext(Dispatchers.IO) {
            val reviews = mutableListOf<GameReviewModel>()
            try {
                val connection = DatabaseManager.getConnection()
                if (connection != null) {
                    val query = "SELECT Utilisateur.UtilisateurID, Utilisateur.Nickname, Avis.Message, Avis.GameName, Avis.AvisTime, Avis.AvisID FROM Avis INNER JOIN Utilisateur ON Avis.UtilisateurID = Utilisateur.UtilisateurID WHERE Avis.GameID = ? ORDER BY AvisTime DESC"
                    val statement = connection.prepareStatement(query)
                    statement.setInt(1, appId)
                    val resultSet = statement.executeQuery()

                    while (resultSet.next()) {
                        val userID = resultSet.getInt("UtilisateurID")
                        val userName = resultSet.getString("Nickname")
                        val reviewMessage = resultSet.getString("Message")
                        val gameName = resultSet.getString("GameName")
                        val reviewDateStr = resultSet.getString("AvisTime")
                        val dateOnly = LocalDateTime.parse(reviewDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
                        val reviewDate = dateOnly.toLocalDate()
                        val reviewID = resultSet.getInt("AvisID")
                        val gameReview = GameReviewModel(userID, userName, gameName, reviewMessage, reviewDate, reviewID)
                        reviews.add(gameReview)
                    }
                    statement.close()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            reviews
        }
    }

    /**
     * Récupère les avis des utilisateurs pour un jeu spécifique depuis la base de données
     *
     * @param userID L'ID de l'utilisateur pour lequel récupérer les avis
     * @return [reviews] Une liste d'objets GameReviewModel représentant les avis des utilisateurs sur le jeu
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchUserGameReviews(userID: Int): List<GameReviewModel> {
        return withContext(Dispatchers.IO) {
            val reviews = mutableListOf<GameReviewModel>()
            try {
                val connection = DatabaseManager.getConnection()
                if (connection != null) {
                    val query = "SELECT Utilisateur.Nickname, Avis.Message, Avis.GameName, Avis.AvisTime, Avis.AvisID FROM Avis INNER JOIN Utilisateur ON Avis.UtilisateurID = Utilisateur.UtilisateurID WHERE Utilisateur.UtilisateurID = ? ORDER BY AvisTime DESC"
                    val statement = connection.prepareStatement(query)
                    statement.setInt(1, userID)
                    val resultSet = statement.executeQuery()

                    while (resultSet.next()) {
                        val userName = resultSet.getString("Nickname")
                        val reviewMessage = resultSet.getString("Message")
                        val gameName = resultSet.getString("GameName")
                        val reviewDateStr = resultSet.getString("AvisTime")
                        val dateOnly = LocalDateTime.parse(reviewDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
                        val reviewDate = dateOnly.toLocalDate()
                        val reviewID = resultSet.getInt("AvisID")
                        val gameReview = GameReviewModel(userID, userName, gameName, reviewMessage, reviewDate, reviewID)
                        reviews.add(gameReview)
                    }
                    statement.close()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            reviews
        }
    }

    /**
     * Supprime définitivement l'avis d'un utilisateur
     *
     * @param userID L'ID de l'utilisateur ayquel l'avis appartient
     * @return [true] si la suppression a réussi, [false] sinon
     */
    suspend fun deleteUserReview(userID: Int, AvisID: Int): Boolean {
        return withContext(Dispatchers.IO) {
            var connection = DatabaseManager.getConnection()
            try {
                if (connection != null) {
                    val deleteReviewQuery = "DELETE FROM Avis WHERE UtilisateurID = ? AND AvisID = ?"
                    val deleteReviewStatement = connection.prepareStatement(deleteReviewQuery)
                    deleteReviewStatement.setInt(1, userID)
                    deleteReviewStatement.setInt(2, AvisID)
                    val isReviewDeleted = deleteReviewStatement.executeUpdate()
                    if (isReviewDeleted > 0) {
                        return@withContext true
                    }
                    deleteReviewStatement.close()
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