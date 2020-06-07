package commanderpepper.getpizza.ui.favorites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import commanderpepper.getpizza.R
import commanderpepper.getpizza.databinding.ActivityFavoritesBinding
import commanderpepper.getpizza.ui.favorites.recyclerview.FavoriteAdapter
import kotlinx.android.synthetic.main.activity_favorites.view.*
import kotlinx.android.synthetic.main.activity_map.view.*

class FavoritesActivity : AppCompatActivity() {

    private lateinit var favViewModel: FavoriteViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_favorites)

        val binding: ActivityFavoritesBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_favorites)

        //Set up the view model
        favViewModel = ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
        val viewManager = LinearLayoutManager(this)

        //Set up the adapter using a list from the data base and instantiate the recycler view
        val favAdapter = FavoriteAdapter(favViewModel.favorites.toList(), this)
        recyclerView = binding.root.fav_list.apply {
            layoutManager = viewManager as RecyclerView.LayoutManager?
            adapter = favAdapter
        }

        val toolbar = binding.favToolbar
        setSupportActionBar(toolbar)
    }
}
