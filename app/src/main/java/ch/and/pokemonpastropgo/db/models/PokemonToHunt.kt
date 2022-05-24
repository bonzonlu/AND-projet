package ch.and.pokemonpastropgo.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PokemonToHunt(
    @PrimaryKey(autoGenerate = true) val huntId: Long?,
    val pokemonId: String,
    val zoneId: Long,
    val hint: String,
    val found: Boolean,
    val lat: Double,
    val lng: Double
)
