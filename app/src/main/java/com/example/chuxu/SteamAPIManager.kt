package com.example.chuxu

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamService {
    @GET("ISteamApps/GetAppList/v2/")
    suspend fun getAppList(): AppListResponse

    @GET("https://store.steampowered.com/api/appdetails")
    suspend fun getAppDetails(@Query("appids") appId: Int): Map<String, GameDetails>
}

data class AppListResponse(
    @SerializedName("applist")
    val appList: AppList
)

data class AppList(
    @SerializedName("apps")
    val apps: List<App>
)

data class App(
    @SerializedName("appid")
    val appId: Int,
    @SerializedName("name")
    val name: String
)

data class GameDetails(
    @SerializedName("data")
    val data: GameData
)

data class GameData(
    @SerializedName("name")
    val name: String,
    @SerializedName("header_image")
    val headerImage: String,
    @SerializedName("short_description")
    val description: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("price_overview")
    val priceOverview: PriceOverview?
)

data class PriceOverview(
    @SerializedName("final_formatted")
    val price: String
)

object SteamAPIManager {
    private const val BASE_URL = "https://api.steampowered.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val steamService: SteamService = retrofit.create(SteamService::class.java)

    // Valeurs pour le délai entre chaque requête --> Pour éviter une erreur 429 de Retrofit2 (TOO MANY REQUESTS)
    private const val REQUEST_DELAY_MS = 2000 // Délai entre chaque requête en millisecondes
    private var lastRequestTime = 0L // Temps de la dernière requête

    suspend fun searchGames(query: String): List<GameData> {
        return withContext(Dispatchers.IO) {
            try {
                // Vérifier si le délai entre les requêtes s'est écoulé
                val currentTime = System.currentTimeMillis()
                val elapsedTimeSinceLastRequest = currentTime - lastRequestTime
                if (elapsedTimeSinceLastRequest < REQUEST_DELAY_MS) {
                    // Attendre le temps restant avant de faire la prochaine requête
                    delay(REQUEST_DELAY_MS - elapsedTimeSinceLastRequest)
                }

                // Enregistrer le temps de la requête actuelle
                lastRequestTime = System.currentTimeMillis()

                // Obtenir la liste des jeux correspondant à la requête de recherche
                val response = steamService.getAppList()
                val games = mutableListOf<GameData>()

                response.appList.apps.forEach { app ->
                    if (app.name.contains(query, ignoreCase = true)) {
                        // Obtenir les détails du jeu
                        val detailsResponse = steamService.getAppDetails(app.appId)
                        val details = detailsResponse[app.appId.toString()]?.data
                        details?.let {
                            games.add(it)
                        }
                    }
                }
                games
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}