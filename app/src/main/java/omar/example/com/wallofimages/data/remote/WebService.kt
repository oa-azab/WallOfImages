package omar.example.com.wallofimages.data.remote


import omar.example.com.wallofimages.data.Image
import retrofit2.Call
import retrofit2.http.GET

interface WebService {

    @GET("raw/wgkJgazE/")
    fun getImages(): Call<List<Image>>

}