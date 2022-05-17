package ch.and.pokemonpastropgo

import androidx.recyclerview.widget.DiffUtil
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone

class ZonesDiffCallback (private val oldList: List<PokemonsFromHuntZone>, private val newList: List<PokemonsFromHuntZone>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].huntZone.zoneId== newList[newItemPosition].huntZone.zoneId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old::class == new::class
    }
}