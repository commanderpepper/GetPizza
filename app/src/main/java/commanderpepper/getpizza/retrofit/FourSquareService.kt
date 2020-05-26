package commanderpepper.getpizza.retrofit

import commanderpepper.getpizza.model.foursquare.SearchResponse
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
        categoryId: String,
        @Query("intent")
        intent: String = "browse",
        @Query("radius")
        radius: Int = 3500,
        @Query("client_id")
        client_id: String = Constants.CLIENT_ID,
        @Query("client_secret")
        client_secret: String = Constants.CLIENT_SECRET,
        @Query("limit")
        limit: Int = 100,
        @Query("v")
        v: String = SimpleDateFormat("yyyyMMdd").format(Date())
    ): SearchResponse

    //    ("client_id:${Constants.CLIENT_ID}", "client_secret:${Constants.CLIENT_SECRET}")
    @GET("venues/search")
    suspend fun searchForTenPizzas(
        @Query("ll")
        ll: String,
        @Query("categoryId")
        categoryId: String,
        @Query("intent")
        intent: String = "browse",
        @Query("radius")
        radius: Int = 3500,
        @Query("client_id")
        client_id: String = Constants.CLIENT_ID,
        @Query("client_secret")
        client_secret: String = Constants.CLIENT_SECRET,
        @Query("limit")
        limit: Int = 10,
        @Query("v")
        v: String = SimpleDateFormat("yyyyMMdd").format(Date())
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
                .baseUrl(FourSquareConstants.FOURSQUARE_BASEURL)
                .client(client)
                .build()

            return retrofit.create(FourSquareService::class.java)
        }

    }
}