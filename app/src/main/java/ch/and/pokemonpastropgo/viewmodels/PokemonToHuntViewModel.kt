package ch.and.pokemonpastropgo.viewmodels

import androidx.lifecycle.*
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import ch.and.pokemonpastropgo.db.repositories.BaseRepository
import ch.and.pokemonpastropgo.db.repositories.PokemonToHuntRepository
import kotlinx.coroutines.flow.Flow

class PokemonToHuntViewModel(rep: BaseRepository): ViewModel() {

    private val repository = rep as PokemonToHuntRepository

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