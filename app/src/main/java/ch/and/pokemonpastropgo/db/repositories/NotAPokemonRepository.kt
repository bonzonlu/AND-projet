package ch.and.pokemonpastropgo.db.repositories

import ch.and.pokemonpastropgo.db.dao.NotAPokemonDAO
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NotAPokemonRepository(
    private val notAPokemonDAO: NotAPokemonDAO,
    private val scope: CoroutineScope
): BaseRepository(scope){
    val allPolemons = notAPokemonDAO.getAllPokemons().distinctUntilChanged()
    val allPokemonCount = notAPokemonDAO.getCount()

    fun insertPokemon(pokemon: NotAPokemon){
        scope.launch(Dispatchers.IO) {
            notAPokemonDAO.insert(pokemon)
        }
    }

}