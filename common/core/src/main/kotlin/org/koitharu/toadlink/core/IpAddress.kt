package org.koitharu.toadlink.core

sealed interface IpAddress : Comparable<IpAddress> {

    data class IPv4(
        val b1: UByte,
        val b2: UByte,
        val b3: UByte,
        val b4: UByte,
    ) : IpAddress {

        override fun compareTo(other: IpAddress): Int {
            if (other !is IPv4) {
                return 1 // TODO support v6
            }
            var result = b1.compareTo(other.b1)
            if (result != 0) return result
            result = b2.compareTo(other.b2)
            if (result != 0) return result
            result = b3.compareTo(other.b3)
            if (result != 0) return result
            return b4.compareTo(other.b4)
        }

        override fun toString(): String = "$b1.$b2.$b3.$b4"
    }

    companion object {

        fun parse(raw: String): IpAddress {
            val parts = raw.split('.')
            require(parts.size == 4) {
                "Cannot parse IP address $raw"
            }
            return IPv4(
                parts[0].toUByte(),
                parts[1].toUByte(),
                parts[2].toUByte(),
                parts[3].toUByte(),
            )
        }
    }
}