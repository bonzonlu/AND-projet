package ch.and.pokemonpastropgo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ch.and.pokemonpastropgo.db.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HuntZoneDAO {
    @Insert
    fun insertAll(vararg huntZone: HuntZone)

    @Insert
    fun insertAll(vararg huntZoneCrossReff: HuntZoneCrossReff)

    @Insert
    fun insert(huntZone: HuntZone)

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
    fun getZone(id: Long): LiveData<PokemonsFromHuntZone>
}