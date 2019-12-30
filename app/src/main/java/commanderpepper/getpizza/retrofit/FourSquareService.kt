package commanderpepper.getpizza.retrofit

import commanderpepper.getpizza.foursquaremodels.SearchResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

interface FourSquareService {

    //    ("client_id:${Constants.CLIENT_ID}", "client_secret:${Constants.CLIENT_SECRET}")
    @GET("venues/search")
    suspend fun searchForPizzas(
        @Query("ll")
        ll: String,
        @Query("categoryId")
        categoryId: String = "4bf58dd8d48988d1ca941735",
        @Query("intent")
        intent: String = "browse",
        @Query("radius")
        radius: Int = 3500,
        @Query("client_id")
        client_id: String = Constants.CLIENT_ID,
        @Query("client_secret")
        client_secret: String = Constants.CLIENT_SECRET,
        @Query("limit")
        limit: Int = 50,
        @Query("v")
        v: String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    ): SearchResponse

    companion object {
        fun create(): FourSquareService {
            val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder().apply {
                this.addInterceptor(interceptor)
            }.build()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(FourSquareConstants.FOURSQUARE_BASEURL)
                .client(client)
                .build()
            return retrofit.create(FourSquareService::class.java)
        }

    }
}