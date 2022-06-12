package ch.and.pokemonpastropgo.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone
import ch.and.pokemonpastropgo.db.repositories.BaseRepository
import ch.and.pokemonpastropgo.db.repositories.HuntZoneRepository

class HuntZonesViewModel(rep: BaseRepository) : ViewModel() {
    private val repository = rep as HuntZoneRepository

    val allZones = repository.allZones
    val zoneCount = repository.allZonesCount

    fun getZone(id: Long): LiveData<PokemonsFromHuntZone> {
        return repository.getZone(id)
    }
}
