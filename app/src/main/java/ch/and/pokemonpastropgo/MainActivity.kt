package ch.and.pokemonpastropgo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.and.pokemonpastropgo.RecyclerViews.ZoneListRecyclerAdapter
import ch.and.pokemonpastropgo.databinding.ActivityMainBinding
import ch.and.pokemonpastropgo.db.PPTGDatabaseApp
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
    private val registerForLocationAndCameraAccess = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            registerForBackGroundAccess.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        else{
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val registerForBackGroundAccess = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()

        if(!authorizedLocation())
            registerForLocationAndCameraAccess.launch( arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
            ))

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

        mainBinding.btnMaps.setOnClickListener {
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.zone_recycler_view)
        val adapter = ZoneListRecyclerAdapter(toHuntVm, this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            vm.allZones.collect {
                adapter.items = it
            }
        }
        vm.zoneCount.observe(this) {
            Log.d("Zone count", it.toString())
        }
    }
}