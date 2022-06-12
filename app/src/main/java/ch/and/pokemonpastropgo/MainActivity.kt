package ch.and.pokemonpastropgo

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.and.pokemonpastropgo.RecyclerViews.ZoneListRecyclerAdapter
import ch.and.pokemonpastropgo.databinding.ActivityMainBinding
import ch.and.pokemonpastropgo.db.PPTGDatabaseApp
import ch.and.pokemonpastropgo.geofencing.MyGeofenceService
import ch.and.pokemonpastropgo.geofencing.MyLocationService
import ch.and.pokemonpastropgo.viewmodels.HuntZonesViewmodel
import ch.and.pokemonpastropgo.viewmodels.PokemonToHuntViewModel
import ch.and.pokemonpastropgo.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding

    private val vm: HuntZonesViewmodel by viewModels {
        ViewModelFactory((application as PPTGDatabaseApp).huntZoneRepository)
    }

    private val toHuntVm: PokemonToHuntViewModel by viewModels {
        ViewModelFactory((application as PPTGDatabaseApp).pokemonToHuntRepository)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val registerForLocationAndCameraAccess =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                registerForBackGroundAccess.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                finish()
            }
        }
    @RequiresApi(Build.VERSION_CODES.Q)
    private val registerForBackGroundAccess =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                doBindService()
            }
        }

    // To invoke the bound service, first make sure that this value
    // is not null.
    private var myLocationService: MyLocationService? = null
    private val locationServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            myLocationService = (service as MyLocationService.LocalBinder).service
            Log.d("MainActivity", "Service Connected ${myLocationService?.mLastLocation}")
            val recyclerView = findViewById<RecyclerView>(R.id.zone_recycler_view)
            val adapter = ZoneListRecyclerAdapter(toHuntVm, this@MainActivity, myLocationService)

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

            lifecycleScope.launch {
                vm.allZones.collect {
                    adapter.items = it
                }
            }
            Log.d("MainActivity", "Service Connected")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            myLocationService = null
            Log.d("MainActivity", "Service Disconnected")
        }
    }

    private var myGeofenceService: MyGeofenceService? = null
    private val geofenceServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            myGeofenceService = (service as MyGeofenceService.LocalBinder).service
            myGeofenceService?.createGlobalGeofenceRequest()
            Log.d("MainActivity", "Service Connected")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            myGeofenceService = null
            Log.d("MainActivity", "Service Disconnected")
        }
    }


    fun doBindService() {
        bindService(
            Intent(this@MainActivity, MyLocationService::class.java),
            locationServiceConnection,
            BIND_AUTO_CREATE
        )
        startService(Intent(this@MainActivity, MyLocationService::class.java))

        bindService(
            Intent(this@MainActivity, MyGeofenceService::class.java),
            geofenceServiceConnection,
            BIND_AUTO_CREATE
        )
        startService(Intent(this@MainActivity, MyGeofenceService::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()

        if (!authorizedLocation())
            registerForLocationAndCameraAccess.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
                )
            )
        else
            doBindService()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "Resume")
        myGeofenceService?.createGlobalGeofenceRequest()

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(locationServiceConnection)
        unbindService(geofenceServiceConnection)
        Log.d("MainActivity", "Service Unbound")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun authorizedLocation(): Boolean {
        val formalizeForeground = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val formalizeBackground =
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
        return formalizeForeground && formalizeBackground
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
    }
}