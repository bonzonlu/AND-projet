package ch.and.pokemonpastropgo.db

import android.app.Application
import ch.and.pokemonpastropgo.db.repositories.HuntZoneRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PPTGDatabaseApp: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val repository by lazy {
        val database = PPTGDatabase.getDB(this)
        HuntZoneRepository(database.huntZoneDAO(), applicationScope)
    }

}