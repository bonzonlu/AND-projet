package ch.and.pokemonpastropgo.geofencing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ch.and.pokemonpastropgo.MapsActivity
import ch.and.pokemonpastropgo.R


fun createChannel(context: Context, channelId: String, channelName: String, importance: Int) {
    val notificationChannel = NotificationChannel(channelId, channelName, importance)
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(notificationChannel)
}

fun NotificationManager.sendGeofenceNotification(context: Context, zoneId: Long, content: String) {
    //Opening the Notification
    val contentIntent = Intent(context, MapsActivity::class.java)
    contentIntent.putExtra("zoneId", zoneId)

    val contentPendingIntent = PendingIntent.getActivity(
        context,
        context.resources.getInteger(R.integer.geofence_notification_id),
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notificationContent: String = content

    //Building the notification
    val builder = NotificationCompat.Builder(
        context,
        context.resources.getString(R.string.geofence_notif_channel_id)
    )
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(notificationContent)
        .setSmallIcon(R.drawable.ic_baseline_location_hint_24)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .build()

    this.notify(R.integer.geofence_notification_id, builder)
}