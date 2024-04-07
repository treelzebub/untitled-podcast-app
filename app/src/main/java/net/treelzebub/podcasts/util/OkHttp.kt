package net.treelzebub.podcasts.util

import okhttp3.OkHttpClient
import okhttp3.Request


fun okHttpClient(fn: OkHttpClient.Builder.() -> Unit): OkHttpClient = OkHttpClient.Builder().apply(fn).build()
fun request(fn: Request.Builder.() -> Unit): Request = Request.Builder().apply(fn).build()
