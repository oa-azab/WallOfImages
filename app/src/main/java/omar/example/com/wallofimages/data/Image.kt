package omar.example.com.wallofimages.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Image(
    @PrimaryKey
    val id: String,
    val color: String,
    @Embedded
    val urls: Url,
    var fetchId: String?
)

data class Url(val raw: String, val full: String, val regular: String, val small: String, val thumb: String)