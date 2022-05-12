package ch.and.pokemonpastropgo.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotAPokemon(
    @PrimaryKey var id: Long,
    var name: String,
    var hint: String,
)
