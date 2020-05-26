package commanderpepper.getpizza.ui.favorites.recyclerview

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import commanderpepper.getpizza.R
import commanderpepper.getpizza.room.entity.PizzaFav

class FavoriteAdapter(private val list: List<PizzaFav>, private val activity: Activity) : RecyclerView.Adapter<FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        return FavoriteViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.fav_item,
                parent, false
            ),
            activity = activity
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(list[position])
    }

}