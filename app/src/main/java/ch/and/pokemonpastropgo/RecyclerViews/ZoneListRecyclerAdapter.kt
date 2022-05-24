package ch.and.pokemonpastropgo.RecyclerViews

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.and.pokemonpastropgo.MapsActivity
import ch.and.pokemonpastropgo.R
import ch.and.pokemonpastropgo.db.models.PokemonsFromHuntZone
import ch.and.pokemonpastropgo.viewmodels.HuntZonesViewmodel
import ch.and.pokemonpastropgo.viewmodels.PokemonToHuntViewModel

class ZoneListRecyclerAdapter(private val model: PokemonToHuntViewModel,
                              private val context: AppCompatActivity,
                              _items: List<PokemonsFromHuntZone> = listOf()) : RecyclerView.Adapter<ZoneListRecyclerAdapter.ViewHolder>() {
    var items = listOf<PokemonsFromHuntZone>()
        set(value) {
            val diffCallback = ZoneListDiffCallback(items, value)
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
        model.pokemonsFoundCntByZone(items[position].huntZone.zoneId!!).observe(context){
            holder.bind(items[position],it)
        }
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val zoneTitle = view.findViewById<TextView>(R.id.zone_title)
        private val zonePokemonCount = view.findViewById<TextView>(R.id.count)


        fun bind(zone: PokemonsFromHuntZone, count: Long){
            view.setOnClickListener {
                val i = Intent(context, MapsActivity::class.java)
                i.putExtra("zoneId",zone.huntZone.zoneId)
                context.startActivity(i)
            }
            zoneTitle.text = zone.huntZone.title
            zonePokemonCount.text = context.getString(R.string.found_pokemons,count,zone.notPokemons.size)

        }
    }
}