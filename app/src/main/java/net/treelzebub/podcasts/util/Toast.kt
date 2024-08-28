package net.treelzebub.podcasts.util

import android.content.Context
import android.widget.Toast

fun toast(c: Context, msg: String, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(c, msg, length).show()

@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("Don't ship without deleting this.")
fun toastNotImplemented(c: Context) = toast(c, "Not implemented.")
