package com.dgmltn.chromis

import android.app.Application
import android.os.AsyncTask
import io.particle.android.sdk.cloud.ParticleCloud
import io.particle.android.sdk.cloud.ParticleCloudException
import io.particle.android.sdk.cloud.ParticleCloudSDK
import io.particle.android.sdk.cloud.ParticleDevice
import timber.log.Timber
import timber.log.Timber.DebugTree


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        ParticleCloudSDK.init(this)

//        Realm.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    companion object {
        val cloud: ParticleCloud by lazy {
            val it = ParticleCloudSDK.getCloud()
            Timber.e("got cloud: ${it.accessToken}")
            try {
                it.logIn(BuildConfig.PARTICLE_USERNAME, BuildConfig.PARTICLE_PASSWORD)
                Timber.e("Logged In")
            }
            catch (e: ParticleCloudException) {
                Timber.e("Could not get cloud: ${e.bestMessage}")
            }
            it
        }

        val device: ParticleDevice by lazy {
            val it = cloud.getDevice(BuildConfig.PARTICLE_IRREMOTE_DEVICE_ID)
            Timber.e("Got Device")
            it
        }
    }

}
