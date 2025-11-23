package org.koitharu.toadlink.storage

import dagger.Reusable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.storage.entity.DeviceEntity
import javax.inject.Inject

@Reusable
class DevicesRepository @Inject internal constructor(
    private val db: ToadDatabase,
) {

    suspend fun get(id: Int): DeviceDescriptor = db.devicesDao.find(id).toDeviceDescriptor()

    fun observeAll(): Flow<ImmutableList<DeviceDescriptor>> = db.devicesDao.observeAll()
        .map { it.map { x -> x.toDeviceDescriptor() }.toImmutableList() }

    suspend fun store(deviceDescriptor: DeviceDescriptor) {
        db.devicesDao.insert(DeviceEntity(deviceDescriptor))
    }
}