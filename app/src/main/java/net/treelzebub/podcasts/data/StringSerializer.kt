package net.treelzebub.podcasts.data


interface StringSerializer<T> {

    fun <T> serialize(input: T): String

    fun <T> deserialize(input: String): T
}
