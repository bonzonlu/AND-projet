package ch.and.pokemonpastropgo.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class UpdatePokemonHint(
    @ColumnInfo(name="huntId")
    val  huntId: String,
    @ColumnInfo(name="displayHint")
    val displayHint: Boolean
)
