package ch.and.pokemonpastropgo.db

import android.app.Application
import ch.and.pokemonpastropgo.db.repositories.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PPTGDatabaseApp: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val repository by lazy {
        val database = PPTGDatabase.getDB(this)
        Repository(database.huntZoneDAO(),database.notAPokemonDAO(),database.pokemonToHuntDAO(), applicationScope)
    }

}