package net.treelzebub.podcasts.util

import okhttp3.Request


fun request(fn: Request.Builder.() -> Unit): Request = Request.Builder().apply(fn).build()
