package org.koitharu.toadlink.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import org.koitharu.toadlink.storage.dao.DevicesDao
import org.koitharu.toadlink.storage.dao.RemoteActionsDao
import org.koitharu.toadlink.storage.entity.DeviceEntity
import org.koitharu.toadlink.storage.entity.RemoteActionEntity

@Database(
    entities = [
        DeviceEntity::class,
        RemoteActionEntity::class,
    ],
    version = 1
)
internal abstract class ToadDatabase : RoomDatabase() {

    abstract val devicesDao: DevicesDao

    abstract val actionsDao: RemoteActionsDao
}