package ch.and.pokemonpastropgo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import ch.and.pokemonpastropgo.databinding.ActivityMainBinding
import ch.and.pokemonpastropgo.databinding.ActivityMapsBinding
import ch.and.pokemonpastropgo.db.PPTGDatabase
import ch.and.pokemonpastropgo.db.PPTGDatabaseApp
import ch.and.pokemonpastropgo.viewmodels.HuntZonesViewmodel
import ch.and.pokemonpastropgo.viewmodels.ViewModelFactory

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding

    private val vm: HuntZonesViewmodel by viewModels{
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

    }
}