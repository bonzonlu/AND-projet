package ch.and.pokemonpastropgo.recyclerViews

import androidx.recyclerview.widget.DiffUtil
import ch.and.pokemonpastropgo.db.models.PokemonToHunt

class HintListDiffCallback(private val oldList: List<PokemonToHunt>, private val newList: List<PokemonToHunt>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].zoneId == newList[newItemPosition].zoneId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return (
                old::class == new::class &&
                        old.zoneId == new.zoneId &&
                        old.displayHint == new.displayHint &&
                        old.found == new.found
                )
    }
}
