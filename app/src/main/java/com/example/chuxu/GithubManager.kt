package com.example.chuxu

import android.util.Log
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Service permettant d'accéder aux fonctionnalités de l'API GitHub
 */
interface GitHubService {
    /**
     * Récupère les informations sur la dernière version de l'application Chuxu depuis GitHub
     */
    @GET("repos/tetrassky/Chuxu/releases")
    suspend fun getLatestRelease(): List<GitHubRelease>
}

/**
 * Modèle de données représentant la dernière version de l'application Chuxu sur GitHub
 * @property tag_name Le numéro de version de la dernière version de l'application
 * @property html_url L'URL de la page de la dernière version de l'application sur GitHub
 */
data class GitHubRelease(
    @SerializedName("tag_name")
    val tag_name: String,
    @SerializedName("html_url")
    val html_url: String
)

/**
 * Gestionnaire pour l'accès à l'API GitHub et la gestion des limitations de requêtes
 */
object GitHubManager {
    private const val BASE_URL = "https://api.github.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(SteamAPIManager.RateLimitInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val githubService: GitHubService = retrofit.create(GitHubService::class.java)

    /**
     * Intercepteur pour journaliser les requêtes et les réponses
     */
    class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            Log.d("GitHubManager", "Sending request: ${request.url}")
            val response: Response = chain.proceed(request)
            Log.d("GitHubManager", "Received response: ${response.code}")
            return response
        }
    }
}