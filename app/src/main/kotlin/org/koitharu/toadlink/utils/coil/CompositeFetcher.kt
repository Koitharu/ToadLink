package org.koitharu.toadlink.utils.coil

import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import org.koitharu.toadlink.core.util.runCatchingCancellable

internal class CompositeFetcher(
    private vararg val delegates: Fetcher
) : Fetcher {

    override suspend fun fetch(): FetchResult? = delegates.firstNotNullOfOrNull { delegate ->
        runCatchingCancellable {
            delegate.fetch()
        }.getOrNull()
    }
}