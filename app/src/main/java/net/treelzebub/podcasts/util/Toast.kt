package net.treelzebub.podcasts.util

import android.content.Context
import android.widget.Toast

fun toast(c: Context, msg: String) = Toast.makeText(c, msg, Toast.LENGTH_SHORT).show()

fun toastNotImplemented(c: Context) = toast(c, "Not implemented.")
