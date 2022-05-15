package ch.and.pokemonpastropgo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ch.and.pokemonpastropgo.db.repositories.HuntZoneRepository
import java.lang.IllegalArgumentException

class ViewModelFactory(private val repository: HuntZoneRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if  (modelClass.isAssignableFrom(HuntZonesViewmodel::class.java)){
            return HuntZonesViewmodel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}