package ch.and.pokemonpastropgo.geofencing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ch.and.pokemonpastropgo.MapsActivity
import ch.and.pokemonpastropgo.R

private const val NOTIFICATION_ID = 33
private const val CHANNEL_ID = "GeofenceChannel"

fun createChannel(context: Context) {
    val notificationChannel = NotificationChannel(CHANNEL_ID, "Channel1", NotificationManager.IMPORTANCE_HIGH)
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(notificationChannel)
}

fun NotificationManager.sendGeofenceNotification(context: Context, enter: Boolean) {
    //Opening the Notification
    val contentIntent = Intent(context, MapsActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notificationContent: String = if (enter) {
        "You have entered a hunting area, start hunting !"
    } else {
        "Go back to the zone to keep hunting !"
    }

    //Building the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(notificationContent)
        .setSmallIcon(R.drawable.ic_baseline_location_hint_24)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .build()

    this.notify(NOTIFICATION_ID, builder)
}