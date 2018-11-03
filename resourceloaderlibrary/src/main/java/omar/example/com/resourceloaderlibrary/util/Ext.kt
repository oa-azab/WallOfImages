package omar.example.com.resourceloaderlibrary.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory


/**
 * Decode ByteArray to bitmap
 */
fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}