package net.treelzebub.podcasts.util

import android.os.Build
import android.os.Build.VERSION_CODES

object DeviceApi {

    val isMinTiramisu: Boolean
        get() = min(VERSION_CODES.TIRAMISU)

    val isAllCakedUp: Boolean
        get() = min(VERSION_CODES.UPSIDE_DOWN_CAKE)

    private fun min(api: Int): Boolean = Build.VERSION.SDK_INT >= api
}