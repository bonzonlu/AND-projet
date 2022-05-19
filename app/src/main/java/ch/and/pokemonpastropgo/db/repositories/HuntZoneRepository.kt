package ch.and.pokemonpastropgo.db.repositories

import androidx.lifecycle.LiveData
import ch.and.pokemonpastropgo.db.dao.HuntZoneDAO
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class HuntZoneRepository(
    private val huntZoneDAO: HuntZoneDAO,
    private val scope: CoroutineScope
): BaseRepository(scope) {
    val allZones = huntZoneDAO.getAllZones().distinctUntilChanged()
    val allZonesCount = huntZoneDAO.getAllZonesCount()

    fun insertZone(zone: HuntZone) {
        scope.launch(Dispatchers.IO) {
            huntZoneDAO.insert(zone)
        }
    }

    fun deleteAll() {
        scope.launch(Dispatchers.IO) {
            huntZoneDAO.deleteAll()
        }
    }

    fun getZone(id: Long): LiveData<PokemonsFromHuntZone> {
        return huntZoneDAO.getZone(id)
    }
}