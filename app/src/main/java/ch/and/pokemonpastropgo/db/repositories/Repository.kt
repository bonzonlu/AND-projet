package ch.and.pokemonpastropgo.db.repositories;

import androidx.lifecycle.LiveData
import ch.and.pokemonpastropgo.db.dao.HuntZoneDAO
import ch.and.pokemonpastropgo.db.dao.NotAPokemonDAO
import ch.and.pokemonpastropgo.db.dao.PokemonToHuntDAO
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class Repository(
    private val huntZoneDAO: HuntZoneDAO,
    private val notAPokemonDAO: NotAPokemonDAO,
    private val pokemonToHuntDAO: PokemonToHuntDAO,
    private val scope: CoroutineScope) {

    val allZones = huntZoneDAO.getAllZones().distinctUntilChanged()
    val allZonesCount = huntZoneDAO.getCount()

    val allPokemonsToHunt =  pokemonToHuntDAO.getAllPokemonsToHunt()
    val allPokemonsToHuntCount = pokemonToHuntDAO.getAllPokemonsToHuntCount()

    val allPokemons = notAPokemonDAO.getAllZones()
    val allPokemonCount = notAPokemonDAO.getCount()


    fun insertZone(zone: HuntZone){
        scope.launch(Dispatchers.IO) {
            huntZoneDAO.insert(zone)
        }
    }

    fun deleteAll(){
        scope.launch(Dispatchers.IO) {
            huntZoneDAO.deleteAll()
        }
    }


    fun insertPokemon(pokemon: NotAPokemon){
        scope.launch(Dispatchers.IO) {
            notAPokemonDAO.insert(pokemon)
        }
    }



    fun insertPokemonToHunt(pokemonToHunt: PokemonToHunt){
        scope.launch(Dispatchers.IO) {
            pokemonToHuntDAO.insertAll(pokemonToHunt)
        }
    }

    fun pokemonsToHuntByZone(id: Long): LiveData<PokemonToHunt>{
        return pokemonToHuntDAO.getAllPokemonsToHuntByZone(id)
    }

    fun pokemonsToHuntCntByZone(id: Long): LiveData<Long> {
        return pokemonToHuntDAO.getPokemonToHuntCountByZone(id)
    }

    fun pokemonsFoundCntByZone(id: Long): LiveData<Long> {
        return pokemonToHuntDAO.getPokemonFoundCountByZone(id)
    }
}
