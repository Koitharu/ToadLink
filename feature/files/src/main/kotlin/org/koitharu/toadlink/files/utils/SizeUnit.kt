package org.koitharu.toadlink.files.utils

enum class SizeUnit(
    private val multiplier: Int,
) {

    BYTES(1), KILOBYTES(1024), MEGABYTES(1024 * 1024);

    fun convert(amount: Long, target: SizeUnit): Long = amount * multiplier / target.multiplier
}