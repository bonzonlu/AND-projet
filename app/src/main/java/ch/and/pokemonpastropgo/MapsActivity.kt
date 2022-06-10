package ch.and.pokemonpastropgo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources.NotFoundException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
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
import ch.and.pokemonpastropgo.geofencing.GeofenceBroadcastReceiver
import ch.and.pokemonpastropgo.geofencing.createChannel
import ch.and.pokemonpastropgo.viewmodels.HuntZonesViewmodel
import ch.and.pokemonpastropgo.viewmodels.PokemonToHuntViewModel
import ch.and.pokemonpastropgo.viewmodels.ViewModelFactory
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// https://github.com/brandy-kay/GeofencingDemo/tree/master/app/src/main/java/com/adhanjadevelopers/geofencingdemo

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private var geofencePref: SharedPreferences? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mapsBinding: ActivityMapsBinding
    private lateinit var geoClient: GeofencingClient
    private val geofenceList = ArrayList<Geofence>()

    private val rotateOpenAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_animation) }
    private val rotateCloseAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_animation) }
    private val fromBottomAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_animation) }
    private val toBottomAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_animation) }

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

    @RequiresApi(Build.VERSION_CODES.Q)
    private val registerForLocationAccess = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            startLocationUpdates()
            registerForBackGroundAccess.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val registerForBackGroundAccess = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        Log.d("PERM", "access to background $it")
    }

    private val getScannedQR = registerForActivityResult(GetScannedQRContract()) {
        Log.d("SCAN QR", "Got QR code $it")
    }

    private val toHuntVm: PokemonToHuntViewModel by viewModels {
        ViewModelFactory((application as PPTGDatabaseApp).pokemonToHuntRepository)
    }
    private val huntZonesVm: HuntZonesViewmodel by viewModels {
        ViewModelFactory((application as PPTGDatabaseApp).huntZoneRepository)
    }

    private var zoneId: Long = -1
    private val gadgetQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private val geofenceIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    var mapFrag: SupportMapFragment? = null
    lateinit var mLocationRequest: LocationRequest
    var mLastLocation: Location? = null
    internal var mFusedLocationClient: FusedLocationProviderClient? = null

    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Log.i("MapsActivity", "Location: " + location.latitude + " " + location.longitude)
                mLastLocation = location

                //move map camera
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11.0F))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handles return arrow button in MapsActivity ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding.root)

        // Notification channel
        createChannel(this)

        // Geofencing
        geoClient = LocationServices.getGeofencingClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Starts the QR code scanner activity when FAB pressed
        mapsBinding.qrCodeScanFab.setOnClickListener {
            getScannedQR.launch(intent.getLongExtra("zoneId", -1))
        }

        mapsBinding.menuFab.setOnClickListener { animateFab() }
        mapsBinding.locationHintFab.setOnClickListener { openPopupWindow() }
        mapsBinding.historyBookFab.setOnClickListener { Toast.makeText(this@MapsActivity, "History book", Toast.LENGTH_SHORT).show() }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFrag?.getMapAsync(this)
    }

    public override fun onPause() {
        super.onPause()

        // Stop location updates when Activity is no longer active
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        zoneId = intent.getLongExtra("zoneId", -1)
        huntZonesVm.getZone(zoneId).observe(this) {
            geofenceList.add(
                Geofence.Builder()
                    .setRequestId("entry.key")
                    .setCircularRegion(it.huntZone.lat, it.huntZone.lng, it.huntZone.radius.toFloat())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            )

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
        }

        startLocationUpdates()
    }

    private fun toBounds(center: LatLng, radius: Double): LatLngBounds {
        val targetNorthEast: LatLng =
            SphericalUtil.computeOffset(center, radius * sqrt(2.0), 45.0)

        val targetSouthWest: LatLng =
            SphericalUtil.computeOffset(center, radius * sqrt(2.0), 225.0)

        return LatLngBounds(targetSouthWest, targetNorthEast)
    }

    private fun startLocationUpdates() {
        if (!mMap.isMyLocationEnabled) {
            // Location and permissions check
            mLocationRequest = create().apply {
                interval = 100
                fastestInterval = 50
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                maxWaitTime = 100
            }

            if ((ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PERMISSION_GRANTED)
            ) {
                //Location Permission already granted
                mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper()!!)
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

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 20
    }

    //specify the geofence to monitor and the initial trigger
    private fun seekGeofencing(): GeofencingRequest? {
        if (geofenceList.size > 0)
            return GeofencingRequest.Builder().apply {
                // FIXME: Fonctionne si on ne suit pas la doc Android. Faire attention au cas où on serait déjà dans la geofence
                // https://developer.android.com/training/location/geofencing#specify-geofences-and-initial-triggers
                //setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                addGeofences(geofenceList)
            }.build()
        else
            return null
    }

    //adding a geofence
    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            return
        }
        val req = seekGeofencing()
        if (req != null) {
            geoClient.addGeofences(req, geofenceIntent).run {
                addOnSuccessListener {
                    Toast.makeText(this@MapsActivity, "Geofences added", Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener {
                    Toast.makeText(this@MapsActivity, "Failed to add geofences", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //removing a geofence
    private fun removeGeofence() {
        geoClient.removeGeofences(geofenceIntent).run {
            addOnSuccessListener {
                Toast.makeText(this@MapsActivity, "Geofences removed", Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener {
                Toast.makeText(this@MapsActivity, "Failed to remove geofences", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun examinePermissionAndInitiateGeofence() {
        if (authorizedLocation()) {
            validateGadgetAreaInitiateGeofence()
        } else {
            askLocationPermission()
        }
    }

    // check if background and foreground permissions are approved
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun authorizedLocation(): Boolean {
        val formalizeForeground = PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val formalizeBackground = PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        return formalizeForeground && formalizeBackground
    }

    //requesting background and foreground permissions
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun askLocationPermission() {
        registerForLocationAccess.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun validateGadgetAreaInitiateGeofence(resolve: Boolean = true) {
        val locationRequest = create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val locationResponses = client.checkLocationSettings(builder.build())

        locationResponses.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        this,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Toast.makeText(this, "Enable your location", Toast.LENGTH_SHORT).show()
            }
        }
        locationResponses.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofence()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()
        geofencePref = getSharedPreferences("TriggerdExitedId", Context.MODE_PRIVATE)
        geofencePref!!.registerOnSharedPreferenceChangeListener(this)
        examinePermissionAndInitiateGeofence()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val triggeredEnterFences: HashSet<String>
        val triggeredGeofences = ArrayList<String>()

        if (key != null) {
            Log.d("onSharedChanged: ", key)
        }

        if (key.equals("geoFenceId")) {
            triggeredEnterFences = geofencePref?.getStringSet("geoFenceId", null) as HashSet<String>

            if (triggeredEnterFences.isEmpty())
                Log.d("onSharedChanged: ", "no exit fences triggered")

            triggeredGeofences.addAll(triggeredEnterFences)

            for (fence in triggeredEnterFences) {
                Log.d("onSharedChanged: ", "ID: $fence triggered!")
                //Here you can call removeGeoFencesFromClient() to unRegister geoFences and removeGeofencesFromMap() to remove marker.
                // removeGeofencesFromClient(triggerdIdList);
                // removeGeofencesFromMap(triggerdIdList);
                if(fence == zoneId.toString()) {
                    mapsBinding.qrCodeScanFab.hide()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeGeofence()
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

    // Floating Action Button
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
}