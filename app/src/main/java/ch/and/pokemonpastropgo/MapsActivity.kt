package ch.and.pokemonpastropgo

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.os.Looper
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ch.and.pokemonpastropgo.databinding.ActivityMapsBinding
import ch.and.pokemonpastropgo.db.PPTGDatabaseApp
import ch.and.pokemonpastropgo.viewmodels.HuntZonesViewmodel
import ch.and.pokemonpastropgo.viewmodels.PokemonToHuntViewModel
import ch.and.pokemonpastropgo.viewmodels.ViewModelFactory
import ch.and.pokemonpastropgo.geofencing.GeofenceBroadcastReceiver
import ch.and.pokemonpastropgo.geofencing.createChannel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.*
import com.google.android.gms.location.LocationRequest

// https://github.com/brandy-kay/GeofencingDemo/tree/master/app/src/main/java/com/adhanjadevelopers/geofencingdemo
private const val TAG = "MapsActivity"
private lateinit var geoClient: GeofencingClient
private val REQUEST_TURN_DEVICE_LOCATION_ON = 20
private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 3
private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 4
private val REQUEST_LOCATION_PERMISSION = 10
private val YVERDON_LAT = 46.77
private val YVERDON_LON = 6.63
private val YVERDON_RAD = 200f

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mapsBinding: ActivityMapsBinding
    private val geofenceList = ArrayList<Geofence>()

    private val rotateOpenAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_animation) }
    private val rotateCloseAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_animation) }
    private val fromBottomAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_animation) }
    private val toBottomAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_animation) }

    private val toHuntVm: PokemonToHuntViewModel by viewModels {
        ViewModelFactory((application as PPTGDatabaseApp).pokemonToHuntRepository)
    }
    private val gadgetQ = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val geofenceIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    var mapFrag: SupportMapFragment? = null
    lateinit var mLocationRequest: LocationRequest
    var mLastLocation: Location? = null
    internal var mCurrLocationMarker: Marker? = null
    internal var mFusedLocationClient: FusedLocationProviderClient? = null

    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude())
                mLastLocation = location
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker?.remove()
                }

                //Place current location marker
                val latLng = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title("Current Position")
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                mCurrLocationMarker = mMap.addMarker(markerOptions)

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

        geofenceList.add(
            Geofence.Builder()
                .setRequestId("entry.key")
                .setCircularRegion(YVERDON_LAT, YVERDON_LON, YVERDON_RAD)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        )

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapsBinding.qrCodeScanFab.setOnClickListener {
            val i = Intent(this, QRCodeActivity::class.java)
            startActivity(i)
        }

        mapsBinding.menuFab.setOnClickListener { animateFab() }
        mapsBinding.locationHintFab.setOnClickListener { Toast.makeText(this@MapsActivity, "Location hint", Toast.LENGTH_SHORT).show() }

        lifecycleScope.launch {
            toHuntVm.pokemonsToHuntByZone(intent.getLongExtra("zoneId", -1)).collect {
                Log.d("", it.size.toString())
            }
        }

        mapsBinding.historyBookFab.setOnClickListener { Toast.makeText(this@MapsActivity, "History book", Toast.LENGTH_SHORT).show() }

        supportActionBar?.title = "Map Location Activity"

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFrag?.getMapAsync(this)
    }

    public override fun onPause() {
        super.onPause()

        //stop location updates when Activity is no longer active
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

    //@SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // https://developers.google.com/maps/documentation/android-sdk/views?hl=fr#setting_boundaries
        // Sets boundaries
        val sw = LatLng(46.30, 6.37)    // SW boundaries - Lausanne
        val ne = LatLng(YVERDON_LAT, YVERDON_LON)    // NE boundaries - Yverdon
        val LausanneYverdonBounds = LatLngBounds(sw, ne)

        // Moves the camera to show the entire area of interest
        mMap.setOnMapLoadedCallback {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(LausanneYverdonBounds, 200))
        }

        val circleOptions = CircleOptions()
            .center(LatLng(YVERDON_LAT, YVERDON_LON))
            .radius(YVERDON_RAD.toDouble())
            .fillColor(0x40ff0000).strokeColor(Color.TRANSPARENT)
            .strokeWidth(2F)

        mMap.addCircle(circleOptions)

        //mMap.mapType = GoogleMap.MAP_TYPE_HYBRID


        mLocationRequest = LocationRequest().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            //Location Permission already granted
            mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper()!!)
            mMap.isMyLocationEnabled = true
        } else {
            //Request Location Permission
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            // Geolocation
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    // Permission was granted, yay! Do the location-related task you need to do
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PERMISSION_GRANTED
                    ) {
                        // TODO : ERROR
                        /*
                        mFusedLocationClient?.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()
                        )
                        mMap.setMyLocationEnabled(true)
                        */
                    }
                } else {
                    // Permission denied, boo! Disable the functionality that depends on that permission
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
            // Geofencing
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && (grantResults[0] == PERMISSION_GRANTED))
                    startLocation()
            }
        }
        // other 'case' lines to check for other
        // permissions this app might request
    }

    /*
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                startLocation()
        }
    }
    */

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) === PERMISSION_GRANTED
    }

    private fun startLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    //specify the geofence to monitor and the initial trigger
    private fun seekGeofencing(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // FIXME: Fonctionne si on ne suit pas la doc Android. Faire attention au cas où on serait déjà dans la geofence
            //setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    //adding a geofence
    private fun addGeofence() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            return
        }
        geoClient?.addGeofences(seekGeofencing(), geofenceIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(this@MapsActivity, "Geofences added", Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener {
                Toast.makeText(this@MapsActivity, "Failed to add geofences", Toast.LENGTH_SHORT).show()

            }
        }
    }

    //removing a geofence
    private fun removeGeofence() {
        geoClient?.removeGeofences(geofenceIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(this@MapsActivity, "Geofences removed", Toast.LENGTH_SHORT).show()

            }
            addOnFailureListener {
                Toast.makeText(this@MapsActivity, "Failed to remove geofences", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun examinePermisionAndinitiatGeofence() {
        if (authorizedLocation()) {
            validateGadgetAreaInitiateGeofence()
        } else {
            askLocationPermission()
        }
    }

    // check if background and foreground permissions are approved
    @TargetApi(29)
    private fun authorizedLocation(): Boolean {
        val formalizeForeground = (
                PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val formalizeBackground =
            if (gadgetQ) {
                PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }
        return formalizeForeground && formalizeBackground
    }

    //requesting background and foreground permissions
    @TargetApi(29)
    private fun askLocationPermission() {
        if (authorizedLocation())
            return
        var grantingPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val customResult = when {
            gadgetQ -> {
                grantingPermission += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(TAG, "askLocationPermission: ")
        ActivityCompat.requestPermissions(
            this,
            grantingPermission,
            customResult
        )
    }

    private fun validateGadgetAreaInitiateGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        validateGadgetAreaInitiateGeofence(false)
    }

    override fun onStart() {
        super.onStart()
        examinePermisionAndinitiatGeofence()
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
}