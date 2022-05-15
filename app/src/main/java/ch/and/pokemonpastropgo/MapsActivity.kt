package ch.and.pokemonpastropgo

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ch.and.pokemonpastropgo.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mapsBinding: ActivityMapsBinding

    private val rotateOpenAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_animation) }
    private val rotateCloseAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_animation) }
    private val fromBottomAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_animation) }
    private val toBottomAnimation: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_animation) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handles return arrow button in MapsActivity ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapsBinding.qrCodeScanFab.setOnClickListener {
            val i = Intent(this, QRCodeActivity::class.java)
            startActivity(i)
        }

        mapsBinding.historyBookFab.setOnClickListener { animateFab() }
        mapsBinding.locationHintFab.setOnClickListener { Toast.makeText(this@MapsActivity, "Location hint", Toast.LENGTH_SHORT).show() }
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // https://developers.google.com/maps/documentation/android-sdk/views?hl=fr#setting_boundaries
        // Sets boundaries
        val sw = LatLng(46.51, 6.63)    // SW boundaries - Lausanne
        val ne = LatLng(46.77, 6.64)    // NE boundaries - Yverdon
        val LausanneYverdonBounds = LatLngBounds(sw, ne)

        // Moves the camera to show the entire area of interest
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(LausanneYverdonBounds, 200));
    }

    private var fabOpen = false
    private fun animateFab() {
        if (fabOpen) {
            mapsBinding.historyBookFab.startAnimation(rotateCloseAnimation)
            mapsBinding.locationHintFab.startAnimation(toBottomAnimation)
            mapsBinding.locationHintFab.visibility = View.VISIBLE
            //mapsBinding.locationHintFab.isClickable = true
            //mapsBinding.locationHintFab.isFocusable = true
        } else {
            mapsBinding.historyBookFab.startAnimation(rotateOpenAnimation)
            mapsBinding.locationHintFab.startAnimation(fromBottomAnimation)
            /*mapsBinding.locationHintFab.visibility = View.INVISIBLE
            mapsBinding.locationHintFab.isClickable = false
            mapsBinding.locationHintFab.isFocusable = false*/
        }
        fabOpen = !fabOpen
        Toast.makeText(this@MapsActivity, fabOpen.toString(), Toast.LENGTH_SHORT).show()
    }
}