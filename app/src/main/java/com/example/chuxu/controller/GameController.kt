package com.example.chuxu.controller

import com.example.chuxu.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GameController {

    // Fonction pour insérer un jeu dans la base de données
    suspend fun insertGame(id: Int, name: String) {
        return withContext(Dispatchers.IO) {
            try {
                val connection = DatabaseManager.getConnection()
                connection?.let {
                        val query = "INSERT INTO Games (GameID, Nom) VALUES (?, ?)"
                        val statement = it.prepareStatement(query)
                        statement.setInt(1, id)
                        statement.setString(2, name)
                        statement.executeUpdate()
                        statement.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}