package ch.and.pokemonpastropgo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources.NotFoundException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.and.pokemonpastropgo.RecyclerViews.HintListRecyclerAdapter
import ch.and.pokemonpastropgo.databinding.ActivityMapsBinding
import ch.and.pokemonpastropgo.db.PPTGDatabaseApp
import ch.and.pokemonpastropgo.geofencing.MyGeofenceService
import ch.and.pokemonpastropgo.geofencing.MyLocationService
import ch.and.pokemonpastropgo.geofencing.createChannel
import ch.and.pokemonpastropgo.viewmodels.HuntZonesViewmodel
import ch.and.pokemonpastropgo.viewmodels.PokemonToHuntViewModel
import ch.and.pokemonpastropgo.viewmodels.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// https://github.com/brandy-kay/GeofencingDemo/tree/master/app/src/main/java/com/adhanjadevelopers/geofencingdemo

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private var geoFencePref: SharedPreferences? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mapsBinding: ActivityMapsBinding
    private var zoneId: Long = -1
    private var zoneName: String = ""

    private val rotateOpenAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_animation) }
    private val rotateCloseAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_animation) }
    private val fromBottomAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_animation) }
    private val toBottomAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_animation) }

    // Obtains code scanned from QR Activity to complete Pokémon in DB (Inner class - contract)
    class GetScannedQRContract : ActivityResultContract<Long, String>() {
        override fun createIntent(context: Context, input: Long): Intent =
            Intent(context, QRCodeActivity::class.java).apply {
                putExtra("zoneId", input)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): String {
            if (resultCode != Activity.RESULT_OK)
                return ""
            return intent?.getStringExtra(QRCodeActivity.SCAN_QR_RESULT_KEY)!!
        }
    }

    private val getScannedQR = registerForActivityResult(GetScannedQRContract()) {
        Log.d("SCAN QR", "Got QR code $it")
    }

    // ViewModels
    private val toHuntVm: PokemonToHuntViewModel by viewModels {
        ViewModelFactory((application as PPTGDatabaseApp).pokemonToHuntRepository)
    }
    private val huntZonesVm: HuntZonesViewmodel by viewModels {
        ViewModelFactory((application as PPTGDatabaseApp).huntZoneRepository)
    }


    private var mapFrag: SupportMapFragment? = null

    // To invoke the bound service, first make sure that this value
    // is not null.
    private var myLocationService: MyLocationService? = null

    private val locationServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            myLocationService = (service as MyLocationService.LocalBinder).service
        }

        override fun onServiceDisconnected(className: ComponentName) {
            myLocationService = null
        }
    }

    private var myGeofenceService: MyGeofenceService? = null

    val getMyGeofenceService = myGeofenceService
    private val geofenceServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            myGeofenceService = (service as MyGeofenceService.LocalBinder).service
            myGeofenceService?.removeGlobalGeofenceRequest()

            Log.d("MainActivity", "Service Connected")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            myGeofenceService = null
            Log.d("MainActivity", "Service Disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handles return arrow button in MapsActivity ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding.root)

        // Notification channel
        createChannel(
            this,
            resources.getString(R.string.geofence_notif_channel_id),
            resources.getString(R.string.geofence_notif_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Starts the QR code scanner activity when FAB pressed
        mapsBinding.qrCodeScanFab.setOnClickListener {
            getScannedQR.launch(intent.getLongExtra("zoneId", -1))
        }

        mapsBinding.menuFab.setOnClickListener { animateFab() }
        mapsBinding.locationHintFab.setOnClickListener { openPopupWindow() }
        mapsBinding.historyBookFab.setOnClickListener {
            Toast.makeText(
                this@MapsActivity,
                "History book",
                Toast.LENGTH_SHORT
            ).show()
        }


        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        bindService(
            Intent(this@MapsActivity, MyLocationService::class.java),
            locationServiceConnection, BIND_AUTO_CREATE
        )
        bindService(
            Intent(this@MapsActivity, MyGeofenceService::class.java),
            geofenceServiceConnection, BIND_AUTO_CREATE
        )

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()
        geoFencePref = getSharedPreferences(
            resources.getString(R.string.geofence_preferences),
            Context.MODE_PRIVATE
        )
        geoFencePref!!.registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            // Customise the styling of the base map using a JSON object defined in a raw resource file.
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        // Gets clicked zone from Main Activity and displays in on the map
        zoneId = intent.getLongExtra("zoneId", -1)
        huntZonesVm.getZone(zoneId).observe(this) {
            zoneName = it.huntZone.title

            myGeofenceService?.createUniqueGeofenceRequest(it)

            mMap.clear()

            // Creates and display a Geofence
            val huntZoneCircle = CircleOptions()
                .center(LatLng(it.huntZone.lat, it.huntZone.lng))
                .radius(it.huntZone.radius)
                .fillColor(0x40ff0000).strokeColor(Color.TRANSPARENT)
                .strokeWidth(2F)

            mMap.addCircle(huntZoneCircle)

            val huntZoneBounds = toBounds(LatLng(it.huntZone.lat, it.huntZone.lng), it.huntZone.radius)

            // Moves the camera to show the entire area of interest
            mMap.setOnMapLoadedCallback {
                val camupdate = CameraUpdateFactory.newLatLngBounds(huntZoneBounds, 200)
                mMap.animateCamera(camupdate)
            }
            supportActionBar?.title = it.huntZone.title

            val distToCheck =
                FloatArray(2) //variable to take distance from our location to center of crcle
            Location.distanceBetween(
                it.huntZone.lat,
                it.huntZone.lng,
                myLocationService!!.location.value?.latitude!!,
                myLocationService!!.location.value?.longitude!!,
                distToCheck
            )
            mapsBinding.qrCodeScanFab.visibility =
                if (distToCheck[0] > it.huntZone.radius) View.GONE else View.VISIBLE

        }

        startLocationUpdates()
    }

    // Returns a LatLngBounds matching the zone of interest
    private fun toBounds(center: LatLng, radius: Double): LatLngBounds {
        val targetNorthEast: LatLng =
            SphericalUtil.computeOffset(center, radius * sqrt(2.0), 45.0)

        val targetSouthWest: LatLng =
            SphericalUtil.computeOffset(center, radius * sqrt(2.0), 225.0)

        return LatLngBounds(targetSouthWest, targetNorthEast)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!mMap.isMyLocationEnabled) {

            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED)
            ) {
                //Location Permission already granted
                mMap.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK") { _, _ ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@MapsActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myGeofenceService?.removeUniqueGeofenceRequest()
    }
    // Handles QR-code scanner button enabling or disabling
    // FIXME
    // https://stackoverflow.com/questions/67416235/how-to-notify-the-calling-activity-from-a-broadcastreceiver-when-using-geofencin
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val triggers: Boolean
        if (key.equals(resources.getString(R.string.location_status))) {
            triggers =
                geoFencePref?.getBoolean(resources.getString(R.string.location_status), false)!!
            mapsBinding.qrCodeScanFab.visibility = if (triggers) View.VISIBLE else View.GONE
        }
    }

    // Handles return arrow button in MapsActivity ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Floating Action Button animations
    private var fabOpen = false
    private fun animateFab() {
        if (fabOpen) {
            mapsBinding.menuFab.startAnimation(rotateCloseAnimation)
            mapsBinding.locationHintFab.startAnimation(toBottomAnimation)
            mapsBinding.historyBookFab.startAnimation(toBottomAnimation)
        } else {
            mapsBinding.menuFab.startAnimation(rotateOpenAnimation)
            mapsBinding.locationHintFab.startAnimation(fromBottomAnimation)
            mapsBinding.historyBookFab.startAnimation(fromBottomAnimation)
        }
        fabOpen = !fabOpen
    }

    // Open a popup window to display hints on Pokémon to hunt
    private fun openPopupWindow() {
        val popupWindow = PopupWindow(this)
        val popupView = layoutInflater.inflate(R.layout.popup_window, null)

        val recyclerView = popupView.findViewById<RecyclerView>(R.id.popup_recycler_view)
        val adapter = HintListRecyclerAdapter(toHuntVm, zoneId, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            toHuntVm.pokemonsToHuntByZone(zoneId).collect {
                println(it.size)
                adapter.items = it
            }
        }
        popupWindow.contentView = popupView
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAtLocation(mapsBinding.root, Gravity.CENTER, 0, 0)
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 20
    }
}