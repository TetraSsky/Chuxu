package com.example.chuxu.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.chuxu.GitHubManager
import com.example.chuxu.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Objet utilitaire pour gérer les mises à jour de l'application Chúxù
 * Cet objet fournit des méthodes pour vérifier les mises à jour et inviter l'utilisateur à mettre à jour l'application
 */
object UpdateUtil {

    /**
     * Vérifie les mises à jour en comparant la version actuelle de l'application avec la dernière version publiée sur GitHub (En relation avec l'API Github)
     * Si une mise à jour est disponible, invite l'utilisateur à mettre à jour l'application
     *
     * @param context Le contexte à partir duquel cette méthode est appelée
     */
    fun checkForUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val releases = GitHubManager.githubService.getLatestRelease()

                if (releases.isNotEmpty()) {
                    val latestRelease = releases.first()

                    val currentVersion = getCurrentVersion(context)
                    val latestVersion = latestRelease.tag_name
                    Log.d("UpdateUtil", "Version actuelle : $currentVersion, Dernière version : $latestVersion")

                    if (currentVersion != latestVersion) {
                        CoroutineScope(Dispatchers.Main).launch {
                            showUpdateDialog(context, latestRelease.html_url)
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "L'application est à jour", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Aucune version de publication n'a été trouvée", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Erreur lors de la vérification des mises à jour", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Récupère la version actuelle de l'application installée sur l'appareil
     *
     * @param context Le contexte à partir duquel cette méthode est appelée
     * @return La version actuelle de l'application sous forme de chaîne de caractères
     */
    private fun getCurrentVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            "1.0"
        }
    }

    /**
     * Affiche une boîte de dialogue invitant l'utilisateur à mettre à jour l'application
     * Si l'utilisateur accepte, il est redirigé vers l'URL pour télécharger la mise à jour
     *
     * @param context Le contexte à partir duquel cette méthode est appelée.
     * @param updateUrl L'URL où la dernière version de l'application peut être téléchargée.
     */
    private fun showUpdateDialog(context: Context, updateUrl: String) {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        alertDialogBuilder.apply {
            setTitle("Nouvelle version Chúxù")
            setMessage("Une nouvelle version de Chúxù est disponible !\nVoulez-vous mettre à jour maintenant ?")
            setPositiveButton("Oui") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                context.startActivity(intent)
            }
            setNegativeButton("Non") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context, R.color.green))
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(context, R.color.red))
    }
}