package org.koitharu.toadlink.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.koitharu.toadlink.core.DeviceDescriptor

@Entity(tableName = "devices")
internal data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", index = true) val id: Int,
    @ColumnInfo("hostname") val hostname: String,
    @ColumnInfo("port") val port: Int,
    @ColumnInfo("alias") val alias: String?,
    @ColumnInfo("username") val username: String,
    @ColumnInfo("password") val password: String,
) {

    constructor(descriptor: DeviceDescriptor) : this(
        id = descriptor.id,
        hostname = descriptor.hostname,
        port = descriptor.port,
        alias = descriptor.alias,
        username = descriptor.username,
        password = descriptor.password
    )

    fun toDeviceDescriptor() = DeviceDescriptor(
        id = id,
        hostname = hostname,
        port = port,
        alias = alias,
        username = username,
        password = password
    )
}