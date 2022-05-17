package ch.and.pokemonpastropgo.db.repositories;

import ch.and.pokemonpastropgo.db.dao.NotAPokemonDAO
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotAPokemonRepository(private val notAPokemonDAO: NotAPokemonDAO, private val scope: CoroutineScope) {
    val allPokemons = notAPokemonDAO.getAllZones()
    val pokemonCount = notAPokemonDAO.getCount()

    fun insertZone(pokemon: NotAPokemon){
        scope.launch(Dispatchers.IO) {
            notAPokemonDAO.insert(pokemon)
        }
    }

    fun deleteAll(){
        scope.launch(Dispatchers.IO) {
            notAPokemonDAO.deleteAll()
        }
    }
}
