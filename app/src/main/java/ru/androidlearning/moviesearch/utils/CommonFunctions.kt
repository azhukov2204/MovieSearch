package ru.androidlearning.moviesearch.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.view.View
import androidx.core.app.NotificationCompat
import com.google.android.material.snackbar.Snackbar
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.model.Movie
import ru.androidlearning.moviesearch.model.db.MovieEntity
import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "yyyy-MM-dd  HH:mm:ss"
fun getStringFromDate(date: Date): String? = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date)

fun View.showSnackBar(
    message: String,
    length: Int = Snackbar.LENGTH_SHORT,
    actionText: String? = null,
    action: ((View) -> Unit)? = null
) {
    Snackbar.make(this, message, length).setAction(actionText, action).show()
}

fun mapMovieEntityToMovie(movieEntity: MovieEntity) = Movie(
    id = movieEntity.id,
    title = movieEntity.title,
    releaseDate = movieEntity.releaseDate,
    rating = movieEntity.rating,
    posterUri = movieEntity.posterUri,
    genre = movieEntity.genre,
    durationInMinutes = movieEntity.durationInMinutes,
    description = movieEntity.description,
    category = "",
    isAdult = movieEntity.isAdult
)

fun createNotificationChannel(applicationContext: Context, channelId: String, channelName: String, channelDescription: String? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showNotification(applicationContext: Context, title: String, message: String, channelId: String, notificationId: Int) {
    val notificationBuilder =
        NotificationCompat.Builder(applicationContext, channelId).apply {
            setSmallIcon(R.drawable.ic_notification)
            setContentTitle(title)
            setContentText(message)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(notificationId, notificationBuilder.build())
}
