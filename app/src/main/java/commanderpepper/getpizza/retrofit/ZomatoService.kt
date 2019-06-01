package commanderpepper.getpizza.retrofit

import com.squareup.moshi.Moshi
import commanderpepper.getpizza.models.SearchResults
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface ZomatoService {
    @Headers("user-key:${Constants.API_KEY}", "cuisines:${ZomatoConstants.PIZZA_CUISINE_ID}")
    @GET("search")
    fun performSearch(@Header("lat") lat: Double, @Header("lan") lan: Double): Observable<SearchResults>

    companion object {
        fun create() : ZomatoService{
            val retrofit = Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ZomatoConstants.ZOMATO_URL)
                .build()
                return retrofit.create(ZomatoService::class.java)
        }
    }

}

