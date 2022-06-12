package ch.and.pokemonpastropgo.geofencing

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import ch.and.pokemonpastropgo.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

private const val TAG = "GeofenceBroadcastReceiver"

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private var geoFencePref: SharedPreferences? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        geoFencePref = context?.getSharedPreferences(
            context.resources.getString(R.string.geofence_preferences),
            Context.MODE_PRIVATE
        )

        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        when (val geofenceTransition = geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {

                val trigger = geofencingEvent.triggeringGeofences
                // Creating and sending Notification
                val notificationManager = ContextCompat.getSystemService(
                    context!!,
                    NotificationManager::class.java
                ) as NotificationManager
                notificationManager.sendGeofenceNotification(
                    context,
                    trigger[0].requestId.toLong(),
                    "You have entered a hunting area, start hunting !"
                )
                geoFencePref?.edit()?.putBoolean(
                    context.resources.getString(
                        R.string.location_status
                    ), true
                )?.apply()

            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                val notificationManager = ContextCompat.getSystemService(
                    context!!,
                    NotificationManager::class.java
                ) as NotificationManager
                notificationManager.sendGeofenceNotification(
                    context,
                    geofencingEvent.triggeringGeofences[0].requestId.toLong(),
                    "Go back to the zone to keep hunting !"
                )
                geoFencePref?.edit()?.putBoolean(
                    context.resources.getString(
                        R.string.location_status
                    ), false
                )?.apply()
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
            }
            else -> {
                Log.e(TAG, "Invalid type transition $geofenceTransition")
            }
        }
    }
}