package ch.and.pokemonpastropgo

import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import ch.and.pokemonpastropgo.databinding.ActivityMainBinding
import ch.and.pokemonpastropgo.databinding.ActivityMapsBinding
import ch.and.pokemonpastropgo.geofencing.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofencelist: MutableList<Geofence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mainBinding.btnMaps.setOnClickListener {
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        }

        geofencingClient = LocationServices.getGeofencingClient(this)

        geofencelist = mutableListOf<Geofence>()
        geofencelist.add(
            (
                    Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId("1")

                        // Set the circular region of this geofence.
                        .setCircularRegion(
                            46.77, 6.65, 200F
                        )

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                        // Create the geofence.
                        .build()
                    )
        )

        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {
            addOnSuccessListener {
                // Geofences added
                // ...
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
            }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofencelist)
        }.build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}