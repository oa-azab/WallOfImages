package omar.example.com.resourceloaderlibrary.model

import okhttp3.ResponseBody
import retrofit2.Call


data class DownloadRequest(
    val url: String,
    val call: Call<ResponseBody>,
    val fetchRequests: MutableList<FetchRequest>
) {
    fun cancel(fetchRequest: FetchRequest) {
        fetchRequests.remove(fetchRequest)
        if (fetchRequests.isEmpty()) call.cancel()
    }
}