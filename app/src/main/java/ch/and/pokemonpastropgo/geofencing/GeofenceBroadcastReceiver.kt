package ch.and.pokemonpastropgo.geofencing

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

private const val TAG = "GeofenceBroadcastReceiver"

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        when (val geofenceTransition = geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d(TAG, "ENTER TRANSITION")
                val triggeringGeofences = geofencingEvent.triggeringGeofences

                /*
                // Obtaining transition details as a String.
                val geofenceTransitionDetails = getGeofenceTransitionDetails(
                    context!!,
                    geofenceTransition,
                    triggeringGeofences
                )*/

                // Creating and sending Notification
                val notificationManager = ContextCompat.getSystemService(
                    context!!,
                    NotificationManager::class.java
                ) as NotificationManager



                notificationManager.sendGeofenceEnteredNotification(context)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d(TAG, "EXIT TRANSITION")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d(TAG, "DWELL TRANSITION")
            }
            else -> {
                Log.e(TAG, "Invalid type transition $geofenceTransition")
            }
        }
    }
}