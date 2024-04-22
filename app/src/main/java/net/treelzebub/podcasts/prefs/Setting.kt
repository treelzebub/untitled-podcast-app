package net.treelzebub.podcasts.prefs

import net.treelzebub.podcasts.prefs.UserSetting.UserDataSyncPlatform.Platform

interface Setting<T> {
    val key: String
    val value: T
    val default: T
}

sealed class AppSetting<T>(
    override val key: String,
    override val value: T,
    override val default: T
) : Setting<T> {
    /**
     * Fetch new episodes in the background every [value] minutes.
     */
    class SyncFrequency(value: Int) : AppSetting<Int>("sync-frequency-minutes", value, 12 * 60)
    class AssociateWithRssFiles(value: Boolean) : AppSetting<Boolean>("associate-with-rss-files", value, true)
    class ProactivelySync(value: Boolean) : AppSetting<Boolean>("proactively-sync", value, true)
    class WarnOnMeteredNetwork(value: Boolean) : AppSetting<Boolean>("warn-on-metered-network", value, false)
    class SyncOnMeteredNetwork(value: Boolean) : AppSetting<Boolean>("sync-on-metered-network", value, true)
}

sealed class PlaybackSetting<T>(
    override val key: String,
    override val value: T,
    override val default: T
) : Setting<T> {
    /**
     * If the user stops playback with seconds of remaining time <= [value], the file is marked as played.
     */
    class EndThreshold(value: Int) : AppSetting<Int>("end-threshold-seconds", value, 30)
    class SkipForwardSeconds(value: Int) : AppSetting<Int>("skip-forward-seconds", value, 30)
    class SkipBackwardSeconds(value: Int) : AppSetting<Int>("skip-backward-seconds", value, 10)
    class RewindOnResumeSeconds(value: Int) : AppSetting<Int>("rewind-on-resume-seconds", value, 10)
    class KeepScreenAwake(value: Boolean) : AppSetting<Boolean>("keep-screen-awake", value, false)
}

sealed class UserSetting<T>(
    override val key: String,
    override val value: T,
    override val default: T
) : Setting<T> {
    class UserDataSyncPlatform(value: Platform) : UserSetting<Platform>("user-data-sync-platform", value, Platform.None) {
        enum class Platform { None, GDrive, Dropbox, Box }
    }
}
