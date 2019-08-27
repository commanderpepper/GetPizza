package commanderpepper.getpizza.ui

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import commanderpepper.getpizza.R

class PizzaInfoWindowAdapter(context: Activity) : GoogleMap.InfoWindowAdapter {

    private val contents: View

    init {
        contents = context.layoutInflater.inflate(
            R.layout.content_bookmark_info, null
        )
    }

    override fun getInfoContents(marker: Marker?): View {
        contents.findViewById<TextView>(R.id.title).text = marker?.title ?: ""
        contents.findViewById<TextView>(R.id.address).text = marker?.snippet ?: ""
        return contents
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

}