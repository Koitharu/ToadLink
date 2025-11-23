package org.koitharu.toadlink.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.koitharu.toadlink.storage.entity.DeviceEntity

@Dao
internal abstract class DevicesDao {

    @Query("SELECT * FROM devices ORDER BY id")
    abstract fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id")
    abstract suspend fun find(id: Int): DeviceEntity

    @Insert
    abstract suspend fun insert(entity: DeviceEntity)
}