package net.treelzebub.podcasts.data

import app.cash.sqldelight.ColumnAdapter

class SerializedColumnAdapter<T : Any>(private val serializer: StringSerializer<T>) : ColumnAdapter<T, String> {

    override fun encode(value: T): String = serializer.serialize(value)

    override fun decode(databaseValue: String): T = serializer.deserialize(databaseValue)
}