package com.dgmltn.chromis

import android.app.Application
import io.particle.android.sdk.cloud.*
import io.particle.android.sdk.utils.Py
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import pl.coreorb.selectiondialogs.data.SelectableIcon
import timber.log.Timber
import timber.log.Timber.DebugTree


class App : Application() {

    val icons by lazy { arrayListOf(
            SelectableIcon("input", getString(R.string.icon_input), R.drawable.ic_button_input),
            SelectableIcon("hdmi", getString(R.string.icon_hdmi), R.drawable.ic_button_input_hdmi),
            SelectableIcon("power", getString(R.string.icon_power), R.drawable.ic_button_power),
            SelectableIcon("remote", getString(R.string.icon_remote), R.drawable.ic_button_remote),
            SelectableIcon("television", getString(R.string.icon_television), R.drawable.ic_button_television),
            SelectableIcon("volume-", getString(R.string.icon_volume_minus), R.drawable.ic_button_volume_minus),
            SelectableIcon("volume+", getString(R.string.icon_volume_plus), R.drawable.ic_button_volume_plus),
            SelectableIcon("mute", getString(R.string.icon_mute), R.drawable.ic_button_mute)
    ) }

    override fun onCreate() {
        super.onCreate()

        ParticleCloudSDK.init(this)

        Realm.init(this)

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
            } catch (e: ParticleCloudException) {
                Timber.e("Could not get cloud: ${e.bestMessage}")
            }
            it
        }

        val device: ParticleDevice by lazy {
            val it = cloud.getDevice(BuildConfig.PARTICLE_IRREMOTE_DEVICE_ID)
            Timber.e("Got Device")
            it
        }

        val particleEventSubject: PublishSubject<ParticleEvent> by lazy {
            val it: PublishSubject<ParticleEvent> = PublishSubject.create()
            Timber.e("creating particleEventSubject")
            Observable
                    .fromCallable {
                        device.subscribeToEvents(
                                null, // eventNamePrefix, optional
                                object : ParticleEventHandler {
                                    override fun onEvent(eventName: String, event: ParticleEvent) {
                                        Timber.i("Received event with payload: " + event.dataPayload)
                                        it.onNext(event)
                                    }

                                    override fun onEventError(e: Exception) {
                                        Timber.e("Event error: ", e)
                                        it.onError(e)
                                    }
                                }
                        )
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            it
        }

        fun particleFunctionCall(name: String, arguments: Any?): Observable<Int> =
                Observable
                        .just(Py.list(arguments?.toString()))
                        .observeOn(Schedulers.io())
                        .map { App.device.callFunction(name, it) }
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnError { Timber.e(it) }
                        .onErrorReturn { -1 }

    }



}
