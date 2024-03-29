package ch.and.pokemonpastropgo.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ch.and.pokemonpastropgo.db.repositories.BaseRepository
import java.lang.IllegalArgumentException

class ViewModelFactory(private val repository: BaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HuntZonesViewModel::class.java)) {
            return HuntZonesViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(PokemonToHuntViewModel::class.java)) {
            return PokemonToHuntViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}