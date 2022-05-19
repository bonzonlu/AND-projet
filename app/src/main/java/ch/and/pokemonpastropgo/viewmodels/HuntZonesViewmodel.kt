package ch.and.pokemonpastropgo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone
import ch.and.pokemonpastropgo.db.repositories.Repository

class HuntZonesViewmodel(private val repository: Repository): ViewModel() {
    val allZones = repository.allZones
    val zoneCount = repository.allZonesCount

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

    fun getZone(id:Long): LiveData<PokemonsFromHuntZone>{
        return repository.getZone(id)
    }
    fun deleteAllZones(){
        repository.deleteAll()
    }
    fun countFound(id: Long?): LiveData<Long> {
        return repository.pokemonsFoundCntByZone(id)
    }


}

