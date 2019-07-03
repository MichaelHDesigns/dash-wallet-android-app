package io.wookey.dash

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import io.wookey.dash.support.VersionManager
import io.wookey.dash.support.extensions.DelegatesExt
import io.wookey.dash.support.extensions.getLocale
import io.wookey.dash.support.extensions.getSystemDefaultLocale
import io.wookey.dash.support.extensions.setLocale
import java.util.*

class App : Application() {

    companion object {
        var SYSTEM_DEFAULT_LOCALE: Locale by DelegatesExt.notNullSingleValue()
        var instance: App by DelegatesExt.notNullSingleValue()
        var newVersion = false
    }

    override fun attachBaseContext(base: Context?) {
        SYSTEM_DEFAULT_LOCALE = getSystemDefaultLocale()
        super.attachBaseContext(base)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        newConfig?.let {
            SYSTEM_DEFAULT_LOCALE = getSystemDefaultLocale(it)
            setLocale(this, getLocale())
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {}

            override fun onActivityResumed(activity: Activity?) {}

            override fun onActivityStarted(activity: Activity?) {}

            override fun onActivityDestroyed(activity: Activity?) {
                ActivityStackManager.getInstance().remove(activity)
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

            override fun onActivityStopped(activity: Activity?) {}

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                ActivityStackManager.getInstance().add(activity)
            }

        })

        VersionManager().getLatestReleases()
    }

}