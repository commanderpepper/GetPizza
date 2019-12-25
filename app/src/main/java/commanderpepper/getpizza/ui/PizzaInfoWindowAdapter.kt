package commanderpepper.getpizza.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import commanderpepper.getpizza.R
import commanderpepper.getpizza.foursquaremodels.Venue

class PizzaInfoWindowAdapter(private val context: Activity) : GoogleMap.InfoWindowAdapter {

    @SuppressLint("InflateParams")
    private val contents: View = context.layoutInflater.inflate(
        R.layout.content_bookmark_info, null
    )

    override fun getInfoContents(marker: Marker?): View {
        contents.findViewById<TextView>(R.id.title).text = marker?.title ?: ""
        contents.findViewById<TextView>(R.id.address).text = marker?.snippet ?: ""

        val pair: Pair<Boolean, Venue> = marker?.tag as Pair<Boolean, Venue>

        Log.d("IsFav", pair.first.toString())

        if (pair.first) {
            contents.findViewById<ImageView>(R.id.favorite).setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    android.R.drawable.star_big_on
                )
            )
        } else {
            contents.findViewById<ImageView>(R.id.favorite).setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    android.R.drawable.star_big_off
                )
            )
        }


        return contents
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

}