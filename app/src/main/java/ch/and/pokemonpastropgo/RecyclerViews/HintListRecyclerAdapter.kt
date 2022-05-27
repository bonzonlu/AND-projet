package ch.and.pokemonpastropgo.RecyclerViews

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.and.pokemonpastropgo.R
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone
import ch.and.pokemonpastropgo.viewmodels.PokemonToHuntViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HintListRecyclerAdapter(
    private val model: PokemonToHuntViewModel,
    private val zone: Long,
    private val context: AppCompatActivity,
    _items: List<PokemonToHunt> = listOf()
): RecyclerView.Adapter<HintListRecyclerAdapter.ViewHolder>() {

    var items = listOf<PokemonToHunt>()
        set(value) {
            val diffCallback = HintListDiffCallback(items, value)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = value
            diffResult.dispatchUpdatesTo(this)
        }

    init {
        items = _items
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.hint_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { holder.bind(items[position])
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val pokemonName = view.findViewById<TextView>(R.id.pokemon_name)
        private val hintText = view.findViewById<TextView>(R.id.hint_text)


        fun bind(item: PokemonToHunt) {
            pokemonName.text = item.pokemonId
            hintText.text = item.hint
        }
    }
}