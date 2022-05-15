package ch.and.pokemonpastropgo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import kotlinx.coroutines.flow.Flow

@Dao
interface HuntZoneDAO {
    @Insert
    fun insert(huntZone: HuntZone): Long

    @Update
    fun update(huntZone: HuntZone)

    @Delete
    fun delete( huntZone: HuntZone)

    @Query("DELETE FROM HuntZone")
    fun deleteAll()

    @Query("Select * FROM HuntZone")
    fun getAllZones(): Flow<List<HuntZone>>

    @Query("Select COUNT(*) FROM HuntZone")
    fun getCount(): LiveData<Long>

    @Query("Select * FROM HuntZone  where :id =zoneId")
    fun getZone(id: Long): LiveData<HuntZone>


}