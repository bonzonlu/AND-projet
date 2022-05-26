package ch.and.pokemonpastropgo.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PokemonToHunt(
    @PrimaryKey
    val huntId: String,
    val pokemonId: String,
    val zoneId: Long,
    val hint: String,
    val displayHint: Boolean,
    var found: Boolean,
    val lat: Double,
    val lng: Double
)
