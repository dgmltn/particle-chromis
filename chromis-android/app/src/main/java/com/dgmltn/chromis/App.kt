package com.dgmltn.chromis

import android.app.Application
import android.os.AsyncTask
import io.particle.android.sdk.cloud.*
import io.particle.android.sdk.utils.Py
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

        val particleEventListener: Observable<ParticleEvent> by lazy {
            var subscriptionId = 0L
            Observable.create({ emitter: ObservableEmitter<ParticleEvent> ->
                subscriptionId = device.subscribeToEvents(
                        null, // eventNamePrefix, optional
                        object : ParticleEventHandler {
                            override fun onEvent(eventName: String, event: ParticleEvent) {
                                Timber.i("Received event with payload: " + event.dataPayload)
                                emitter.onNext(event)
                            }

                            override fun onEventError(e: Exception) {
                                Timber.e("Event error: ", e)
                            }
                        })
            })
                    .doOnDispose {
                        App.device.unsubscribeFromEvents(subscriptionId)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }

        fun particleFunctionCall(name: String, arguments: Any?): Observable<Int> =
                Observable
                        .fromCallable { App.device.callFunction(name, Py.list(arguments?.toString())) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())


    }

}
