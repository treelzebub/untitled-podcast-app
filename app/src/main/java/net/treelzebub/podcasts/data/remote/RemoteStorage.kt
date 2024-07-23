package net.treelzebub.podcasts.data.remote

import kotlinx.coroutines.Deferred

interface RemoteStorage<T> {
    fun save(data: T): Deferred<Result<T>>
    fun load() : Deferred<Result<T>>
}
