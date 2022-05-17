package ch.and.pokemonpastropgo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ch.and.pokemonpastropgo.db.models.HuntZoneCrossReff
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone
import kotlinx.coroutines.flow.Flow

@Dao
interface NotAPokemonDAO {
    @Insert
    fun insertAll(vararg notAPokemonDAO: NotAPokemon)

    @Insert
    fun insert( notAPokemonDAO: NotAPokemon)

    @Update
    fun update(notAPokemonDAO: NotAPokemon)

    @Delete
    fun delete( notAPokemonDAO: NotAPokemon)

    @Query("DELETE FROM NotAPokemon")
    fun deleteAll()

    @Query("Select * FROM NotAPokemon")
    fun getAllZones(): Flow<List<NotAPokemon>>

    @Query("Select COUNT(*) FROM NotAPokemon")
    fun getCount(): LiveData<Long>

    @Query("Select * FROM NotAPokemon LEFT JOIN HuntZone ON HuntZone.zoneId = NotAPokemon.pokemonId where HuntZone.zoneId = :id")
    fun getPokemonsFromZone(id: Long): LiveData<PokemonsFromHuntZone>

}