package ch.and.pokemonpastropgo.geofencing

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import ch.and.pokemonpastropgo.R
import ch.and.pokemonpastropgo.db.PPTGDatabaseApp
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone
import ch.and.pokemonpastropgo.db.repositories.HuntZoneRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MyGeofenceService : Service() {

    lateinit var zones: HuntZoneRepository
    lateinit var scope: CoroutineScope

    inner class LocalBinder : Binder() {
        val service: MyGeofenceService
            get() = this@MyGeofenceService
    }

    val mBinder: IBinder = LocalBinder()

    private lateinit var geoClient: GeofencingClient
    private val geofenceList = ArrayList<Geofence>()

    lateinit var zoneTitles: ArrayList<String>
    lateinit var geofenceRequest: GeofencingRequest


    val gobalPendingIntent: PendingIntent by lazy {
        val intent = Intent(this@MyGeofenceService, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            this@MyGeofenceService,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    val uniquePendingIntent: PendingIntent by lazy {
        val intent = Intent(this@MyGeofenceService, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            this@MyGeofenceService,
            1,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("MissingPermission")
    fun createUniqueGeofenceRequest(zone: PokemonsFromHuntZone) {
        geofenceList.clear()
        Log.d("MyGeofenceService", "createUniqueGeofenceRequest ${zone}")
        geofenceList.add(
            Geofence.Builder()
                .setRequestId(zone.huntZone.zoneId.toString())
                .setCircularRegion(
                    zone.huntZone.lat,
                    zone.huntZone.lng,
                    zone.huntZone.radius.toFloat()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        )
        geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(geofenceList)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this@MyGeofenceService,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geoClient.addGeofences(geofenceRequest, uniquePendingIntent)
                .addOnSuccessListener {
                    Log.d("MyGeofenceService", "Geofence added")
                }
                .addOnFailureListener {
                    Log.d("MyGeofenceService", "Geofence failed")
                }
        }

    }


    @SuppressLint("MissingPermission")
    fun createGlobalGeofenceRequest() {
        scope.launch {
            zones.allZones.collect { zoneList ->
                Log.d("MyGeofenceService", "createGlobalGeofenceRequest ${zoneList.size}")
                geofenceList.clear()
                zoneList.forEach {
                    geofenceList.add(
                        Geofence.Builder()
                            .setRequestId(it.huntZone.zoneId.toString())
                            .setCircularRegion(
                                it.huntZone.lat,
                                it.huntZone.lng,
                                it.huntZone.radius.toFloat()
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build()
                    )

                    Log.d("GeofenceService", "Geofence added: ${it.huntZone.zoneId}")
                }
                geofenceRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(0)
                    .addGeofences(geofenceList)
                    .build()

                if (ActivityCompat.checkSelfPermission(
                        this@MyGeofenceService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    geoClient.addGeofences(geofenceRequest, gobalPendingIntent)
                        .addOnSuccessListener {
                            Log.d("MyGeofenceService", "Geofence added")
                        }
                        .addOnFailureListener {
                            Log.d("MyGeofenceService", "Geofence failed")
                        }
                }
                //execute only once
                this.coroutineContext.cancel()
            }
        }
    }

    fun removeGlobalGeofenceRequest() {
        geoClient.removeGeofences(gobalPendingIntent)
    }

    fun removeUniqueGeofenceRequest() {
        geoClient.removeGeofences(uniquePendingIntent)
    }

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(SupervisorJob())
        zones = (application as PPTGDatabaseApp).huntZoneRepository
        geoClient = LocationServices.getGeofencingClient(this)
        zoneTitles = arrayListOf()
        createChannel(
            this,
            resources.getString(R.string.geofence_notif_channel_id),
            resources.getString(R.string.geofence_notif_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onDestroy() {
        removeGlobalGeofenceRequest()
        removeUniqueGeofenceRequest()
    }


}

