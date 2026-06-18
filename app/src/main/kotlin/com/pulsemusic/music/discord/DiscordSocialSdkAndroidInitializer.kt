/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.discord

import android.app.Activity
import timber.log.Timber

object DiscordSocialSdkAndroidInitializer {
    private const val TAG = "DiscordSocialSdkInit"

    fun setEngineActivity(activity: Activity) {
        runCatching {
            val initClass = Class.forName("com.discord.socialsdk.DiscordSocialSdkInit")
            val method = initClass.getMethod("setEngineActivity", Activity::class.java)
            method.invoke(null, activity)
        }.onFailure {
            Timber.tag(TAG).v(it, "Discord Social SDK Android init class is not available")
        }
    }
}
