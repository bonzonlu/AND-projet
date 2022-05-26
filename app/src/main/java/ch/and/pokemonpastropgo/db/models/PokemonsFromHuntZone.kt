package ch.and.pokemonpastropgo.db.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PokemonsFromHuntZone(
    @Embedded val huntZone: HuntZone,
    @Relation(
        parentColumn = "zoneId",
        entityColumn = "huntId",
        associateBy = Junction(PokemonToHunt::class)
    )
    val notPokemons: List<PokemonToHunt>,
)
