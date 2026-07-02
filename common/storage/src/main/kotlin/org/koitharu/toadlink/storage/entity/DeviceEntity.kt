package org.koitharu.toadlink.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koitharu.toadlink.core.DeviceDescriptor
import kotlin.time.Instant

@Entity(
    tableName = "devices",
    indices = [
        Index("hostname", "port", "username", unique = true)
    ]
)
internal data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", index = true) val id: Int,
    @ColumnInfo("hostname") val hostname: String,
    @ColumnInfo("port") val port: Int,
    @ColumnInfo("alias") val alias: String?,
    @ColumnInfo("username") val username: String,
    @ColumnInfo("password") val password: String,
    @ColumnInfo("key") val key: String?,
    @ColumnInfo("last_connect") val lastConnect: Long,
    @ColumnInfo("auto_connect") val connectAutomatically: Boolean,
) {

    constructor(descriptor: DeviceDescriptor) : this(
        id = descriptor.id,
        hostname = descriptor.hostname,
        port = descriptor.port,
        alias = descriptor.alias,
        username = descriptor.username,
        password = descriptor.password,
        lastConnect = descriptor.lastConnect?.toInstant(TimeZone.UTC)?.toEpochMilliseconds() ?: 0L,
        key = descriptor.key,
        connectAutomatically = descriptor.connectAutomatically,
    )

    fun toDeviceDescriptor() = DeviceDescriptor(
        id = id,
        hostname = hostname,
        port = port,
        alias = alias,
        username = username,
        password = password,
        lastConnect = if (lastConnect == 0L) {
            null
        } else {
            Instant.fromEpochMilliseconds(lastConnect).toLocalDateTime(TimeZone.UTC)
        },
        key = key,
        connectAutomatically = connectAutomatically,
    )
}