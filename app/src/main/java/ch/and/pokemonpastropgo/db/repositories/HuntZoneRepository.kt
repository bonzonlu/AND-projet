package ch.and.pokemonpastropgo.db.repositories;

import ch.and.pokemonpastropgo.db.dao.HuntZoneDAO
import ch.and.pokemonpastropgo.db.models.HuntZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HuntZoneRepository(private val huntZoneDAO: HuntZoneDAO, private val scope: CoroutineScope) {
    val allZones = huntZoneDAO.getAllZones()
    val zonesCount = huntZoneDAO.getCount()

    fun insertZone(zone: HuntZone){
        scope.launch(Dispatchers.IO) {
            huntZoneDAO.insert(zone)
        }
    }

    fun deleteAll(){
        scope.launch(Dispatchers.IO) {
            huntZoneDAO.deleteAll()
        }
    }

}
