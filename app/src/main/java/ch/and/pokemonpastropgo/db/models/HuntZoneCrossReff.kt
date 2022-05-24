package ch.and.pokemonpastropgo.db.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.NotAPokemon

@Entity(primaryKeys = ["zoneId","huntId"])
data class HuntZoneCrossReff(
    val zoneId: Long,
    val huntId: Long,
)
