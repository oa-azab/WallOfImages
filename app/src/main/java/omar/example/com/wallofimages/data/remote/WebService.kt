package omar.example.com.wallofimages.data.remote


import omar.example.com.wallofimages.data.Image
import omar.example.com.wallofimages.util.Const.REMOTE_PAGE_SIZE
import omar.example.com.wallofimages.util.Const.UNSPLASH_API_KEY
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WebService {

    @GET("raw/wgkJgazE/")
    fun getImages(): Call<List<Image>>

    @GET("https://api.unsplash.com/photos/")
    fun getImagesFromUnsplash(
        @Query("page") page: Int = 1,
        @Query("client_id") apiKey: String = UNSPLASH_API_KEY,
        @Query("per_page") perPage: Int = REMOTE_PAGE_SIZE
    ): Call<List<Image>>
}