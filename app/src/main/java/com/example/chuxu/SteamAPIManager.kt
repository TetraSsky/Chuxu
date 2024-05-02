package com.example.chuxu

import com.example.chuxu.SteamAPIManager.RateLimitInterceptor.CALL_INTERVAL
import com.example.chuxu.SteamAPIManager.RateLimitInterceptor.REQUEST_INTERVAL
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.System.currentTimeMillis

/**
 * Service pour accéder aux fonctionnalités de l'API Steam.
 */
interface SteamService {
    /**
     * Obtenir la liste des applications Steam.
     */
    @GET("ISteamApps/GetAppList/v2/")
    suspend fun getAppList(): AppListResponse

    /**
     * Obtenir les détails d'une application Steam.
     *
     * @param appId Identifiant de l'application.
     * @return Les détails de l'application.
     */
    @GET("https://store.steampowered.com/api/appdetails")
    suspend fun getAppDetails(@Query("appids") appId: Int): Response<Map<String, GameDetails>>
}

/**
 * Réponse de la liste des applications Steam.
 */
data class AppListResponse(
    @SerializedName("applist")
    val appList: AppList
)

/**
 * Liste des applications Steam.
 */
data class AppList(
    @SerializedName("apps")
    val apps: List<App>
)

/**
 * Application Steam.
 */
data class App(
    @SerializedName("appid")
    val appId: Int,
    @SerializedName("name")
    val name: String
)

/**
 * Détails d'un jeu Steam.
 */
data class GameDetails(
    @SerializedName("data")
    val data: GameData
)

/**
 * Données d'un jeu Steam.
 */
data class GameData(
    @SerializedName("appid")
    val appId: Int,
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

/**
 * Vue d'ensemble du prix d'un jeu Steam.
 */
data class PriceOverview(
    @SerializedName("final_formatted")
    val price: String
)

/**
 * Gestionnaire de l'API Steam.
 */
object SteamAPIManager {
    private const val BASE_URL = "https://api.steampowered.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(RateLimitInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Service pour accéder aux fonctionnalités de l'API Steam.
     */
    val steamService: SteamService = retrofit.create(SteamService::class.java)

    /**
     * Intercepteur pour limiter le débit des requêtes vers l'API Steam.
     * Cet intercepteur garantit que le nombre de requêtes ne dépasse pas la limite spécifiée.
     */
    object RateLimitInterceptor : Interceptor {
        const val REQUEST_LIMIT = 200
        const val REQUEST_INTERVAL = 120000L
        const val CALL_INTERVAL = 1000L

        private val requestQueue = mutableListOf<Long>()

        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val currentTime = currentTimeMillis()

            requestQueue.removeAll { currentTime - it > REQUEST_INTERVAL }

            while (requestQueue.size >= REQUEST_LIMIT) {
                Thread.sleep(1000)
                requestQueue.removeAll { currentTime - it > REQUEST_INTERVAL }
            }
            requestQueue.add(currentTime)
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
                val response = steamService.getAppList()
                val games = mutableListOf<GameData>()

                response.appList.apps.forEach { app ->
                    if (app.name.contains(query, ignoreCase = true)) {

                        val detailsResponse = steamService.getAppDetails(app.appId)

                        if (detailsResponse.isSuccessful) {
                            val responseBody = detailsResponse.body()
                            responseBody?.let { body ->
                                val details = body[app.appId.toString()]?.data
                                details?.let {
                                    games.add(it.copy(appId = app.appId))
                                }
                            }
                        } else if (detailsResponse.code() == 429) {
                            delay(REQUEST_INTERVAL)
                            val detailsResponseRetry = steamService.getAppDetails(app.appId)
                            val responseBody = detailsResponseRetry.body()
                            responseBody?.let { body ->
                                val details = body[app.appId.toString()]?.data
                                details?.let {
                                    games.add(it.copy(appId = app.appId))

                                }
                            }
                        }
                        delay(CALL_INTERVAL)
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