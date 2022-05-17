package ch.and.pokemonpastropgo.db.repositories

import ch.and.pokemonpastropgo.db.dao.PokemonToHuntDAO
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PokemonToHuntRepository(private val pokemonToHuntDAO: PokemonToHuntDAO, private val scope: CoroutineScope) {

    val allPokemonsToHunt = pokemonToHuntDAO.getAllPokemonsToHunt()
    val allPokemonsToHuntCount = pokemonToHuntDAO.getAllPokemonsToHuntCount()

    fun insertPokemonToHunt(pokemonToHunt: PokemonToHunt){
        scope.launch(Dispatchers.IO) {
            pokemonToHuntDAO.insertAll(pokemonToHunt)
        }
    }

}