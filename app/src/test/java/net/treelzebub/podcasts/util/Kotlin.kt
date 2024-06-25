package net.treelzebub.podcasts.util

import kotlin.test.assertTrue


fun <E> assertEmpty(collection: Collection<E>) = assertTrue { collection.isEmpty() }

fun <E> assertNotEmpty(collection: Collection<E>) = assertTrue { collection.isNotEmpty() }
