package ch.and.pokemonpastropgo.db.models

import androidx.room.Embedded
import androidx.room.Relation
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.NotAPokemon

data class HuntZoneEntry(
    @Embedded val huntZoneL: HuntZone,
    @Relation(
        parentColumn = "huntZoneId",
        entityColumn = "notAPokemonId"
    )
    val notPokemons: List<NotAPokemon>
)
