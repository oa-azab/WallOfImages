package omar.example.com.resourceloaderlibrary

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface DownloadService {
    @GET
    fun downloadResource(@Url fileUrl: String): Call<ResponseBody>
}