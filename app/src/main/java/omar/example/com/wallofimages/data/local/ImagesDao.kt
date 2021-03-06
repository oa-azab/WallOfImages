package omar.example.com.wallofimages.data.local

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import omar.example.com.wallofimages.data.Image

@Dao
interface ImagesDao {

    @Query("SELECT * FROM Image")
    fun getAll(): DataSource.Factory<Int, Image>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(images: List<Image>)

    @Query("DELETE FROM Image")
    fun deleteImages()

}