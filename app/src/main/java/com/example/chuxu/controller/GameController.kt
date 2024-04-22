package com.example.chuxu.controller

import com.example.chuxu.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GameController {

    // Fonction pour insérer un jeu dans la base de données
    suspend fun insertGame(id: Int, name: String, platformId: Int, type: String) {
        return withContext(Dispatchers.IO) {
            try {
                val connection = DatabaseManager.getConnection()
                connection?.let {
                    val query = "SELECT COUNT(*) FROM Games WHERE GameID=?"
                    val statement = it.prepareStatement(query)
                    statement.setInt(1, id)
                    val result = statement.executeQuery()

                    if (!result.next()) {
                        val query = "INSERT INTO Games (GameID, Nom, PlateformeID, GameType) VALUES (?, ?, ?, ?)"
                        val statement = it.prepareStatement(query)
                        statement.setInt(1, id)
                        statement.setString(2, name)
                        statement.setInt(3, platformId)
                        statement.setString(4, type)
                        statement.executeUpdate()
                        statement.close()
                    } else {
                        statement.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}