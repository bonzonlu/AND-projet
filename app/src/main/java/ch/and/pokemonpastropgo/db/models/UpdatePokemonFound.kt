package ch.and.pokemonpastropgo.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class UpdatePokemonFound(
    @ColumnInfo(name="huntId")
    val  huntId: String,
    @ColumnInfo(name="found")
    val found: Boolean
)
