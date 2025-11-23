package org.koitharu.toadlink.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Insert
    abstract suspend fun insert(entity: RemoteActionEntity)

    @Update
    abstract suspend fun update(entity: RemoteActionEntity)

    @Query("DELETE FROM actions WHERE id = :id")
    abstract suspend fun delete(id: Int)
}