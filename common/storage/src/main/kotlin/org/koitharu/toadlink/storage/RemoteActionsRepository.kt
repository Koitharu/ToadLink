package org.koitharu.toadlink.storage

import dagger.Reusable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.storage.entity.RemoteActionEntity
import javax.inject.Inject

@Reusable
class RemoteActionsRepository @Inject internal constructor(
    private val db: ToadDatabase,
) {

    fun observeAll(deviceId: Int): Flow<ImmutableList<RemoteAction>> = db.actionsDao.observeAll(deviceId)
        .map { it.map { x -> x.toRemoteAction() }.toImmutableList() }

    suspend fun store(action: RemoteAction, deviceId: Int) {
        db.actionsDao.upsert(
            RemoteActionEntity(
                action = action,
                deviceId = deviceId,
            )
        )
    }
}