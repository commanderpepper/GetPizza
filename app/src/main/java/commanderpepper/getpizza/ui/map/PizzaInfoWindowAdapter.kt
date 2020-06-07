package commanderpepper.getpizza.ui.map

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import commanderpepper.getpizza.R
import commanderpepper.getpizza.model.foursquare.Venue
import commanderpepper.getpizza.room.entity.PizzaFav
import timber.log.Timber

class PizzaInfoWindowAdapter(private val context: Activity) : GoogleMap.InfoWindowAdapter {

    @SuppressLint("InflateParams")
    private val contents: View = context.layoutInflater.inflate(
        R.layout.content_bookmark_info, null
    )

    override fun getInfoContents(marker: Marker?): View {
        contents.findViewById<TextView>(R.id.title).text = marker?.title ?: ""
        contents.findViewById<TextView>(R.id.address).text = marker?.snippet ?: ""

        contents.findViewById<ImageView>(R.id.favorite).setOnClickListener {
            Timber.d("Favorite clicked")
        }

        val pizzaFav = marker?.tag as PizzaFav

        if (pizzaFav.favorite == 0) {
            contents.findViewById<ImageView>(R.id.favorite).setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    android.R.drawable.star_big_off
                )
            )
        } else {
            contents.findViewById<ImageView>(R.id.favorite).setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    android.R.drawable.star_big_on
                )
            )
        }

        return contents
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

}