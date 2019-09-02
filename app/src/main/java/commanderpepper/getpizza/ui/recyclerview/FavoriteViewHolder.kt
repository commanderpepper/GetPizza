package commanderpepper.getpizza.ui.recyclerview

import androidx.recyclerview.widget.RecyclerView
import commanderpepper.getpizza.databinding.FavItemBinding
import commanderpepper.getpizza.room.entity.PizzaFav

class FavoriteViewHolder(val binding: FavItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(pizzaFav: PizzaFav) {
        binding.favName.text = pizzaFav.name
        binding.favAddress.text = pizzaFav.address
    }

}