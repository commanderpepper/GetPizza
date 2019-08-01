package commanderpepper.getpizza.retrofit

import commanderpepper.getpizza.foursquaremodels.SearchResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface FourSquareService {

    //    ("client_id:${Constants.CLIENT_ID}", "client_secret:${Constants.CLIENT_SECRET}")
    @GET("search")
    suspend fun search(
        @Query("ll")
        ll: String,
        @Query("categoryId")
        category: String,
        @Query("intent")
        intent: String = "browse",
        @Query("radius")
        radius: Int = 3500,
        @Header("Authorization")
        client_id: String = Constants.CLIENT_ID,
        @Header("Authorization")
        client_secret: String = Constants.CLIENT_SECRET
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