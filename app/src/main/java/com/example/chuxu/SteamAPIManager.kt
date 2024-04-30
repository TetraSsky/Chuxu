package com.example.chuxu

import com.example.chuxu.SteamAPIManager.RateLimitInterceptor.REQUEST_INTERVAL
import com.example.chuxu.SteamAPIManager.RateLimitInterceptor.REQUEST_LIMIT
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit

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

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(RateLimitInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // Utiliser le client OkHttp configuré avec l'intercepteur
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val steamService: SteamService = retrofit.create(SteamService::class.java)


    /**
     * Intercepteur pour limiter le débit des requêtes vers l'API Steam.
     * Cet intercepteur garantit que le nombre de requêtes ne dépasse pas la limite spécifiée.
     */
    object RateLimitInterceptor : Interceptor {
        const val REQUEST_LIMIT = 200 // Limite de requêtes par minute
        const val REQUEST_INTERVAL = 120000L // Intervalle entre les requêtes en millisecondes

        private val requestQueue = mutableListOf<Long>()

        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val currentTime = System.currentTimeMillis()

            requestQueue.removeAll { currentTime - it > REQUEST_INTERVAL }

            while (requestQueue.size >= REQUEST_LIMIT) {
                Thread.sleep(1000) // Attendre une seconde
                requestQueue.removeAll { currentTime - it > REQUEST_INTERVAL }
            }

            requestQueue.add(currentTime)

            // Continuer avec la requête normalement
            return chain.proceed(chain.request())
        }
    }


    /**
     * Recherche des jeux sur Steam correspondant à un certain terme de recherche.
     *
     * @param query Terme de recherche pour trouver des jeux.
     * @return Liste des détails des jeux correspondant à la recherche.
     */
    suspend fun searchGames(query: String): List<GameData> {
        return withContext(Dispatchers.IO) {
            try {
                // Obtenir la liste des jeux correspondant à la requête de recherche
                val response = steamService.getAppList()
                val games = mutableListOf<GameData>()

                response.appList.apps.forEach { app ->
                    if (app.name.contains(query, ignoreCase = true)) {
                        println("Searching for game with appId: ${app.appId}")
                        delay(1000)
                        // Obtenir les détails du jeu
                        val detailsResponse = steamService.getAppDetails(app.appId)
                        val details = detailsResponse[app.appId.toString()]?.data
                        println("Scrapped data: $details")
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