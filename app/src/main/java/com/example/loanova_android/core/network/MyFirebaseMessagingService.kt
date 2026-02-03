package com.example.loanova_android.core.network

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.example.loanova_android.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // TODO: Send token to backend
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received: ${message.notification?.body}")
        Log.d("FCM", "Data payload: ${message.data}")
        
        val loanApplicationId = message.data["loanApplicationId"]
        val notificationType = message.data["type"]
        
        message.notification?.let {
            showNotification(
                title = it.title ?: "Notification",
                body = it.body ?: "",
                loanApplicationId = loanApplicationId,
                type = notificationType
            )
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        loanApplicationId: String?,
        type: String?
    ) {
        val channelId = "loanova_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Create intent to open MainActivity with deep link data
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigateTo", "loan_history")
            loanApplicationId?.let { putExtra("loanApplicationId", it) }
            type?.let { putExtra("notificationType", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Ganti dengan icon app Anda nanti
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // Since android Oreo notification channel is needed.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Loanova Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
