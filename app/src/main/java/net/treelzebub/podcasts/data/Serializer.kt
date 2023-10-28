package net.treelzebub.podcasts.data

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


interface Serializer<T> {

    fun <T> serialize(input: T): String

    fun <T> deserialize(input: String): T
}

class MoshiSerializer<T>(private val clazz: Class<T>) : Serializer<T> {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    override fun <T> serialize(input: T): String {
        val moshi = moshi.adapter<T>(clazz)
        return moshi.toJson(input)
    }

    @Throws(JsonDataException::class)
    override fun <T> deserialize(input: String): T {
        val moshi = moshi.adapter<T>(clazz)
        return moshi.fromJson(input)!!
    }
}
