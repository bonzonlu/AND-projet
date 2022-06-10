package ch.and.pokemonpastropgo.geofencing

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

private const val TAG = "GeofenceBroadcastReceiver"

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private var geofencePref: SharedPreferences? = null
    private val triggeredEnterGeofenceIds: HashSet<String> = HashSet()
    private var triggedGeofenceIdsList: ArrayList<String> = ArrayList()

    override fun onReceive(context: Context?, intent: Intent?) {
        geofencePref = context?.getSharedPreferences("TriggeredExitedId", Context.MODE_PRIVATE)

        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        when (val geofenceTransition = geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d(TAG, "ENTER TRANSITION")

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                storeGeofenceTransitionDetails(geofenceTransition, triggeringGeofences)

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

                notificationManager.sendGeofenceNotification(context, true)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d(TAG, "EXIT TRANSITION")

                // Creating and sending Notification
                val notificationManager = ContextCompat.getSystemService(
                    context!!,
                    NotificationManager::class.java
                ) as NotificationManager

                notificationManager.sendGeofenceNotification(context, false)
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d(TAG, "DWELL TRANSITION")
            }
            else -> {
                Log.e(TAG, "Invalid type transition $geofenceTransition")
            }
        }
    }

    private fun storeGeofenceTransitionDetails(geofenceTransition: Int, triggeredGeofences: List<Geofence>) {
        triggeredEnterGeofenceIds.clear()
        for (geofence in triggeredGeofences) {
            triggedGeofenceIdsList.add(geofence.requestId)

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                triggeredEnterGeofenceIds.add(geofence.requestId)
            }
        }
        geofencePref?.edit()?.putStringSet("geoFenceId", triggeredEnterGeofenceIds)?.apply()
    }
}