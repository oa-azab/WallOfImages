package omar.example.com.wallofimages.data.remote

import android.util.Log
import omar.example.com.wallofimages.data.Image
import omar.example.com.wallofimages.util.Const.BASE_URL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class RemoteDataSource private constructor() {

    private val service: WebService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(WebService::class.java)
    }

    fun fetchImages(page: Int = 1, callback: (NetworkResult<List<Image>>) -> Unit) {
        service.getImagesFromUnsplash(page).enqueue(object : Callback<List<Image>> {
            override fun onFailure(call: Call<List<Image>>, t: Throwable) {
                Log.d(TAG, "[onFailure]")
                callback.invoke(NetworkError(t))
            }

            override fun onResponse(call: Call<List<Image>>, response: Response<List<Image>>) {
                Log.d(TAG, "[onResponse]")
                if (response.isSuccessful) {
                    Log.d(TAG, "[onResponse] isSuccessful")
                    val images = response.body()
                    images?.let {

                        callback.invoke(NetworkSuccess(it))

                        Log.d(TAG, "[onResponse] images size = ${it.size}")
                        it.forEach { Log.d(TAG, "[onResponse] ${it.urls.raw}") }
                    }
                } else {
                    Log.d(TAG, "[onResponse] Not Successful")
                    callback.invoke(NetworkError(IOException("response failed code: ${response.code()}")))
                }
            }

        })
    }

    companion object {
        private const val TAG = "RemoteDataSource"
        private var INSTANCE: RemoteDataSource? = null

        fun getInstance(): RemoteDataSource {
            if (INSTANCE == null) {
                synchronized(RemoteDataSource::class.java) {
                    INSTANCE = RemoteDataSource()
                }
            }
            return INSTANCE!!
        }
    }
}