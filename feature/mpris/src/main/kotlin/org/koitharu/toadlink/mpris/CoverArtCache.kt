package org.koitharu.toadlink.mpris

import android.graphics.Bitmap
import android.util.LruCache

internal class CoverArtCache : LruCache<String, Bitmap>(4) {

    override fun entryRemoved(
        evicted: Boolean,
        key: String?,
        oldValue: Bitmap?,
        newValue: Bitmap?,
    ) {
        super.entryRemoved(evicted, key, oldValue, newValue)
        oldValue?.recycle()
    }
}