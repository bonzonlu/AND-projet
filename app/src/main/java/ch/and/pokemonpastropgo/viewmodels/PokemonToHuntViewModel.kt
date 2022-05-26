package ch.and.pokemonpastropgo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import ch.and.pokemonpastropgo.db.repositories.BaseRepository
import ch.and.pokemonpastropgo.db.repositories.PokemonToHuntRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.lang.UnsupportedOperationException

class PokemonToHuntViewModel(rep: BaseRepository): ViewModel() {

    private val repository = rep as PokemonToHuntRepository
    val allPokemonToHunt = repository.allPokemonsToHunt
    val pokemonToHuntCount = repository.allPokemonsToHuntCount

    fun pokemonsToHuntCntByZone(id: Long): LiveData<Long> {
        return repository.pokemonsToHuntCntByZone(id)
    }

    fun pokemonsFoundCntByZone(id: Long): LiveData<Long> {
        return repository.pokemonsFoundCntByZone(id)
    }

    fun pokemonsToHuntByZone(id: Long): Flow<List<PokemonToHunt>>{
        return repository.pokemonsToHuntByZone(id)
    }

    fun foundPokemon(qrStr: String){
        repository.foundPokemon(qrStr)
    }

    fun displayPokemonHint(huntId: String){
        repository.displayPokemonHint(huntId)
    }


    
}