package org.koitharu.toadlink.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.koitharu.toadlink.core.RemoteAction

@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["device_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
internal data class RemoteActionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", index = true) val id: Int,
    @ColumnInfo(name = "device_id", index = true) val deviceId: Int,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("cmdline") val cmdline: String,
) {

    constructor(action: RemoteAction, deviceId: Int) : this(
        id = action.id,
        name = action.name,
        cmdline = action.cmdline,
        deviceId = deviceId,
    )

    fun toRemoteAction() = RemoteAction(
        id = id,
        name = name,
        cmdline = cmdline,
    )
}