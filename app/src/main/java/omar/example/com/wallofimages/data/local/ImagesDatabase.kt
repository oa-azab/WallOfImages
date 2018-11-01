package omar.example.com.wallofimages.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import omar.example.com.wallofimages.data.Image

@Database(entities = arrayOf(Image::class), version = 1)
abstract class ImagesDatabase : RoomDatabase() {

    abstract fun imagesDao(): ImagesDao

    companion object {

        private var INSTANCE: ImagesDatabase? = null

        private val sLock = Any()

        fun getInstance(context: Context): ImagesDatabase {
            synchronized(sLock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        ImagesDatabase::class.java
                    ).build()
                }
                return INSTANCE!!
            }
        }
    }
}