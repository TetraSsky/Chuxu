package com.example.chuxu.util

import android.os.AsyncTask
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Objet chargé de l'envoi des emails (Code de confirmation)
 */
object EmailUtil {

    /**
     * Envoi un email grâce aux paramètres définis
     *
     * @param toEmail L'adresse email du destinataire
     * @param subject Le sujet de l'email
     * @param body Le corps de l'email
     */

    fun sendEmail(toEmail: String, subject: String, body: String) {
        val props = Properties()
        props["mail.smtp.host"] = "smtp-chuxu.alwaysdata.net"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"

        val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("chuxu@alwaysdata.net", "u=6tmUO=FLbTsk2wW2004")
            }
        })

        val task = object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    val mm = MimeMessage(session)
                    mm.setFrom(InternetAddress("ChuxuTeam@alwaysdata.net"))
                    mm.setRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                    mm.subject = subject
                    mm.setText(body)
                    Transport.send(mm)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
        task.execute()
    }
}