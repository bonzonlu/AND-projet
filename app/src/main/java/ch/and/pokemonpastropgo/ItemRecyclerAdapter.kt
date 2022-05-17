package ch.and.pokemonpastropgo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone
import ch.and.pokemonpastropgo.viewmodels.HuntZonesViewmodel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.coroutines.coroutineContext

class ItemRecyclerAdapter(private val model: HuntZonesViewmodel,private val lifecycleOwner: LifecycleOwner, _items: List<PokemonsFromHuntZone> = listOf()) : RecyclerView.Adapter<ItemRecyclerAdapter.ViewHolder>() {
    var items = listOf<PokemonsFromHuntZone>()
        set(value) {
            val diffCallback = ZonesDiffCallback(items, value)
            val diffItems = DiffUtil.calculateDiff(diffCallback)
            field = value
            diffItems.dispatchUpdatesTo(this)
        }

    init {
        items = _items
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.zone_row_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        model.countFound(items[position].huntZone.zoneId).observe(lifecycleOwner){
            holder.bind(items[position],it)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val zoneTitle = view.findViewById<TextView>(R.id.zone_title)
        private val zonePokemonCount = view.findViewById<TextView>(R.id.count)
        fun bind(zone: PokemonsFromHuntZone, count: Long){
            zoneTitle.text = zone.huntZone.title
            zonePokemonCount.text = count.toString()+"/"+zone.notPokemons.size

        }
    }
}