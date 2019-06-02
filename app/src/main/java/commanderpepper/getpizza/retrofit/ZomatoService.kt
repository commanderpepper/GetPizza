package commanderpepper.getpizza.retrofit

import com.squareup.moshi.Moshi
import commanderpepper.getpizza.models.SearchResults
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface ZomatoService {
    @Headers("user-key:${Constants.API_KEY}")
    @GET("search")
    fun performSearch(
        @Query("lat") lat: Double, @Query("lon") lon: Double, @Query("count") count: Int = 10,
        @Query("cuisines") cuisines: Int = ZomatoConstants.PIZZA_CUISINE_ID
    )
            : Observable<SearchResults>

    companion object {
        fun create(): ZomatoService {
            val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder().apply {
                this.addInterceptor(interceptor)
            }.build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ZomatoConstants.ZOMATO_URL)
                .client(client)
                .build()
            return retrofit.create(ZomatoService::class.java)
        }
    }

}

