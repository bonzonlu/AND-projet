package ch.and.pokemonpastropgo.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HuntZone (
    @PrimaryKey(autoGenerate = true) var zoneId: Long?,
    var title: String,
    var description: String,
    var lat: Double,
    var lng: Double,
    var radius: Double
)