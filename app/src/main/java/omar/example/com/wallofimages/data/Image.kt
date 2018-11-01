package omar.example.com.wallofimages.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Image(
    @PrimaryKey
    val id: String,
    @Embedded
    val urls: Url
)

data class Url(val raw: String, val full: String, val regular: String)