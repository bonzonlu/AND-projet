package ch.and.pokemonpastropgo.db.repositories

import androidx.lifecycle.LiveData
import ch.and.pokemonpastropgo.db.dao.PokemonToHuntDAO
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class PokemonToHuntRepository(
    private  val pokemonToHuntDAO: PokemonToHuntDAO,
    private val scope: CoroutineScope
):BaseRepository(scope) {

    val allPokemonsToHunt =  pokemonToHuntDAO.getAllPokemonsToHunt().distinctUntilChanged()
    val allPokemonsToHuntCount = pokemonToHuntDAO.getAllPokemonsToHuntCount()

    fun insertPokemonToHunt(pokemonToHunt: PokemonToHunt){
        scope.launch(Dispatchers.IO) {
            pokemonToHuntDAO.insertAll(pokemonToHunt)
        }
    }

    fun pokemonsToHuntByZone(id: Long): Flow<List<PokemonToHunt>> {
        return pokemonToHuntDAO.getAllPokemonsToHuntByZone(id)
    }

    fun pokemonsToHuntCntByZone(id: Long): LiveData<Long> {
        return pokemonToHuntDAO.getPokemonToHuntCountByZone(id)
    }

    fun pokemonsFoundCntByZone(id: Long?): LiveData<Long> {
        return pokemonToHuntDAO.getPokemonFoundCountByZone(id)
    }

}