package ch.and.pokemonpastropgo.geofencing

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import ch.and.pokemonpastropgo.R
import com.google.android.gms.location.*

class MyLocationService : Service() {
    private lateinit var notificationManager: NotificationManager
    var mLastLocation: Location? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mLocationRequest: LocationRequest

    inner class LocalBinder : Binder() {
        val service: MyLocationService
            get() = this@MyLocationService
    }

    private val mBinder: IBinder = LocalBinder()
    var location = MutableLiveData<Location>()

    private var myLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                if (location.value == null)
                    location.postValue(locationList.last())
                else {
                    if (locationList.last().distanceTo(location.value) > 50) {
                        location.postValue(locationList.last())
                    }
                }
            }
        }
    }

    override fun onCreate() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        createChannel(
            this,
            resources.getString(R.string.location_notif_channel_id),
            resources.getString(R.string.location_notif_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        showNotification()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // Location and permissions check
        mLocationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }

        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            //Location Permission already granted
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest,
                myLocationCallback,
                Looper.myLooper()!!
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationClient?.removeLocationUpdates(myLocationCallback)
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(
            this,
            resources.getString(R.string.location_notif_channel_id)
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("Pokemon pas trop go is now using your location")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        startForeground(resources.getInteger(R.integer.location_notification_id), builder)
    }
}
