package ch.and.pokemonpastropgo.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotAPokemon(
    @PrimaryKey val pokemonId: String,
    val description: String
)
