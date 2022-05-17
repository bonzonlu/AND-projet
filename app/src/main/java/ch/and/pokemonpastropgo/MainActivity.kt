package ch.and.pokemonpastropgo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.and.pokemonpastropgo.databinding.ActivityMainBinding
import ch.and.pokemonpastropgo.db.PPTGDatabaseApp
import ch.and.pokemonpastropgo.viewmodels.HuntZonesViewmodel
import ch.and.pokemonpastropgo.viewmodels.PokemonToHuntViewModel
import ch.and.pokemonpastropgo.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding

    private val vm: HuntZonesViewmodel by viewModels{
        ViewModelFactory((application as PPTGDatabaseApp).repository)
    }

    private val toHuntVm: PokemonToHuntViewModel by viewModels{
        ViewModelFactory((application as PPTGDatabaseApp).repository)
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

        val recyclle = findViewById<RecyclerView>(R.id.zone_recycler_view)
        val adapter = ItemRecyclerAdapter(vm,this)

        recyclle.adapter = adapter
        recyclle.layoutManager  = LinearLayoutManager(this)

        lifecycleScope.launch {
            vm.allZones.collect {
                adapter.items = it
            }
        }
    }
}