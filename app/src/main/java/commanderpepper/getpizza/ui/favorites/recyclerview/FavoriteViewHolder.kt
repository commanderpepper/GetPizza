package commanderpepper.getpizza.ui.favorites.recyclerview

import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import commanderpepper.getpizza.databinding.FavItemBinding
import commanderpepper.getpizza.room.entity.PizzaFav


class FavoriteViewHolder(
    private val binding: FavItemBinding,
    private val activity: Activity
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(pizzaFav: PizzaFav) {
        binding.favName.text = pizzaFav.name
        binding.favAddress.text = pizzaFav.address
        binding.goToLocationButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("lat", pizzaFav.lat)
            intent.putExtra("lng", pizzaFav.lng)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }

    }
}
