package org.koitharu.toadlink.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.koitharu.toadlink.storage.entity.RemoteActionEntity

@Dao
internal abstract class RemoteActionsDao {

    @Query("SELECT * FROM actions WHERE device_id IS NULL OR device_id = :deviceId")
    abstract fun observeAll(deviceId: Int): Flow<List<RemoteActionEntity>>

    @Query("SELECT * FROM actions WHERE device_id IS NULL")
    abstract fun observeAllCommon(): Flow<List<RemoteActionEntity>>

    @Query("SELECT * FROM actions WHERE id = :id")
    abstract suspend fun get(id: Int): RemoteActionEntity

    @Upsert
    abstract suspend fun upsert(entity: RemoteActionEntity)

    @Query("DELETE FROM actions WHERE id = :id")
    abstract suspend fun delete(id: Int)
}