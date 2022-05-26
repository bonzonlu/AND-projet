package ch.and.pokemonpastropgo.db.dao

import androidx.room.*
import ch.and.pokemonpastropgo.db.models.NotAPokemon
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
    fun getAllPokemons(): Flow<List<NotAPokemon>>

    @Query("Select COUNT(*) FROM NotAPokemon")
    fun getCount(): Flow<Long>
    
}