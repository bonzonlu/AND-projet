package ch.and.pokemonpastropgo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import ch.and.pokemonpastropgo.db.models.UpdatePokemonFound
import ch.and.pokemonpastropgo.db.models.UpdatePokemonHint
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonToHuntDAO {
    @Insert
    fun insertAll(vararg pokemonToHunt: PokemonToHunt)

    @Update(entity = PokemonToHunt::class)
    fun foundPokemon(pokemonToHunt: UpdatePokemonFound)

    @Update(entity = PokemonToHunt::class)
    fun displayHint(pokemonToHunt: UpdatePokemonHint)

    @Query("Select * FROM PokemonToHunt")
    fun getAllPokemonsToHunt(): Flow<List<PokemonToHunt>>

    @Query("SELECT * FROM PokemonToHunt WHERE PokemonToHunt.zoneId = :zoneId AND PokemonToHunt.pokemonId = :id")
    fun getPokemonToHunt(id: String,zoneId: Long): LiveData<PokemonToHunt>

    @Query("SELECT * FROM PokemonToHunt WHERE PokemonToHunt.zoneId = :id")
    fun getAllPokemonsToHuntByZone(id: Long): Flow<List<PokemonToHunt>>

    @Query("Select COUNT(*) FROM PokemonToHunt")
    fun getAllPokemonsToHuntCount(): LiveData<Long>

    @Query("SELECT COUNT(*) FROM PokemonToHunt WHERE PokemonToHunt.zoneId = :id ")
    fun getPokemonToHuntCountByZone(id: Long?): LiveData<Long>

    @Query("SELECT COUNT(*) FROM PokemonToHunt WHERE PokemonToHunt.zoneId = :id AND PokemonToHunt.found=1")
    fun getPokemonFoundCountByZone(id: Long?): LiveData<Long>
}