package ch.and.pokemonpastropgo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import ch.and.pokemonpastropgo.db.repositories.Repository

class PokemonToHuntViewModel(private val repository: Repository): ViewModel() {
    val allPokemonToHunt = repository.allPokemonsToHunt
    val pokemonToHuntCount = repository.allPokemonsToHuntCount

    fun pokemonsToHuntCntByZone(id: Long): LiveData<Long> {
        return repository.pokemonsToHuntCntByZone(id)
    }

    fun pokemonsFoundCntByZone(id: Long): LiveData<Long> {
        return repository.pokemonsFoundCntByZone(id)
    }

    fun pokemonsToHuntByZone(id: Long): LiveData<List<PokemonToHunt>>{
        return repository.pokemonsToHuntByZone(id)
    }
    
}