package omar.example.com.resourceloaderlibrary.model

import android.graphics.Bitmap
import omar.example.com.resourceloaderlibrary.ResourceLoadResult
import java.util.*

data class FetchRequest(
    val url: String,
    val callback: (ResourceLoadResult<Bitmap>) -> Unit,
    val id: String = UUID.randomUUID().toString()
)
