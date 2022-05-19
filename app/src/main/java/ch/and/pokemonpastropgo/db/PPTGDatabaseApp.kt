package ch.and.pokemonpastropgo.db

import android.app.Application
import ch.and.pokemonpastropgo.db.repositories.HuntZoneRepository
import ch.and.pokemonpastropgo.db.repositories.NotAPokemonRepository
import ch.and.pokemonpastropgo.db.repositories.PokemonToHuntRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PPTGDatabaseApp: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val notApokemonRepository by lazy{
        val database = PPTGDatabase.getDB(this)
        NotAPokemonRepository(database.notAPokemonDAO(),applicationScope)
    }

    val pokemonToHuntRepository by lazy {
        val database = PPTGDatabase.getDB(this)
        PokemonToHuntRepository(database.pokemonToHuntDAO(),applicationScope)
    }

    val huntZoneRepository by lazy{
        val database = PPTGDatabase.getDB(this)
        HuntZoneRepository(database.huntZoneDAO(),applicationScope)
    }
}