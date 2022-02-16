package ru.androidlearning.moviesearch.push_notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ru.androidlearning.moviesearch.utils.createNotificationChannel
import ru.androidlearning.moviesearch.utils.showNotification

const val FCM_LOG_TAG = "FCMLog"
private const val NEW_TOKEN_MESSAGE = "New token:"
private const val PUSH_NOTIFICATION_CHANNEL_ID = "PUSH_NOTIFICATION_CHANNEL_ID"
private const val PUSH_NOTIFICATION_CHANNEL_NAME = "PUSH NOTIFICATION CHANNEL"
private const val PUSH_NOTIFICATION_CHANNEL_DESCRIPTION = "Push notification channel"
private const val DEFAULT_PUSH_NOTIFICATION_TITLE = "Data received "
private const val DEFAULT_EMPTY_STRING = ""

class FCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(FCM_LOG_TAG, "$NEW_TOKEN_MESSAGE $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: DEFAULT_PUSH_NOTIFICATION_TITLE
        val originalMessage = remoteMessage.notification?.body ?: DEFAULT_EMPTY_STRING
        val resultMessage = StringBuilder(originalMessage)
        val remoteMessageData = remoteMessage.data
        if (remoteMessageData.isNotEmpty()) {
            remoteMessageData.toMap().let {
                for (key in it.keys) {
                    resultMessage.append(", $key - ${it[key]}")
                }
            }
        }
        createNotificationChannel(applicationContext, PUSH_NOTIFICATION_CHANNEL_ID, PUSH_NOTIFICATION_CHANNEL_NAME, PUSH_NOTIFICATION_CHANNEL_DESCRIPTION)
        showNotification(applicationContext, title, resultMessage.toString(), PUSH_NOTIFICATION_CHANNEL_ID, notificationId++)
    }

    companion object {
        private var notificationId = 0
    }
}
