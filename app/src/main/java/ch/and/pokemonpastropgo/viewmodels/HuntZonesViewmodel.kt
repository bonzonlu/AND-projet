package ch.and.pokemonpastropgo.viewmodels

import androidx.lifecycle.ViewModel
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.repositories.HuntZoneRepository
import ch.and.pokemonpastropgo.db.repositories.NotAPokemonRepository

class HuntZonesViewmodel(private val repository: HuntZoneRepository): ViewModel() {
    val allZones = repository.allZones
    val zoneCount = repository.zonesCount

    fun createZone(title: String, description: String, lat: Double, lng: Double, radius: Double){
        val zone = HuntZone(
            null,
            title,
            description,
            lat,
            lng,
            radius
        )
        repository.insertZone(zone)
    }

    fun deleteAllZones(){
        repository.deleteAll()
    }
}

