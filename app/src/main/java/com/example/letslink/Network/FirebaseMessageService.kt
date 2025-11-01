package com.example.letslink.Network

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService(){
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Received message: ${remoteMessage.from}")
        remoteMessage.notification?.let{
            showNotification(it.title, it.body)
        }
        if(remoteMessage.data.isNotEmpty()){
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
        }

    }
    private fun showNotification(title: String?, body: String?){
        val channelID = "default_chanel"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelID,
                "default_chanel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

        }
        val builder = NotificationCompat.Builder(this,channelID)
            .setSmallIcon(R.drawable.ic_notification_overlay)
            .setContentTitle(title ?: "LetsLink")
            .setContentText(body ?:"")
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED){
            with(NotificationManagerCompat.from(this)){
                notify(System.currentTimeMillis().toInt(),builder.build())
            }
        }

    }
}