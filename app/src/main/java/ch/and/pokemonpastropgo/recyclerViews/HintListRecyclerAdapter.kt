package ch.and.pokemonpastropgo.recyclerViews

import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.and.pokemonpastropgo.R
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import ch.and.pokemonpastropgo.viewModels.PokemonToHuntViewModel
import jp.wasabeef.blurry.Blurry

class HintListRecyclerAdapter(
    private val model: PokemonToHuntViewModel,
    private val zone: Long,
    private val context: AppCompatActivity,
    _items: List<PokemonToHunt> = listOf()
) : RecyclerView.Adapter<HintListRecyclerAdapter.ViewHolder>() {

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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val pokemonName = view.findViewById<TextView>(R.id.pokemon_name)
        private val hintText = view.findViewById<TextView>(R.id.hint_text)
        private val pokemonIcon = view.findViewById<ImageView>(R.id.pokemon_icon)

        // Binds Pokémon to hunt in the hints list
        fun bind(item: PokemonToHunt) {
            pokemonName.text = item.pokemonId
            hintText.text = item.hint
            val pokemonResourceId = context.resources.getIdentifier(item.pokemonId, "drawable", context.packageName)

            if (!item.found) {
                val bmpImg = BitmapFactory.decodeResource(context.resources, pokemonResourceId)
                val txtBlurFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
                hintText.paint.maskFilter = txtBlurFilter
                Blurry.with(context).radius(25).sampling(8).from(bmpImg).into(pokemonIcon)
            } else {
                pokemonIcon.setImageResource(pokemonResourceId)
            }

            view.setOnClickListener {
                hintText.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                hintText.paint.maskFilter = null
            }
        }
    }
}