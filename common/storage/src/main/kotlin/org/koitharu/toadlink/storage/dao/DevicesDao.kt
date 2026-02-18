package org.koitharu.toadlink.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.koitharu.toadlink.storage.entity.DeviceEntity

@Dao
internal abstract class DevicesDao {

    @Query("SELECT * FROM devices ORDER BY id")
    abstract fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id")
    abstract suspend fun find(id: Int): DeviceEntity

    @Query("DELETE FROM devices WHERE id = :id")
    abstract suspend fun delete(id: Int)

    @Upsert
    abstract suspend fun upsert(entity: DeviceEntity): Long
}