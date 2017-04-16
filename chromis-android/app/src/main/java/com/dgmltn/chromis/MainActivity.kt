package com.dgmltn.chromis

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import io.particle.android.sdk.cloud.ParticleEvent
import io.particle.android.sdk.cloud.ParticleEventHandler
import io.particle.android.sdk.utils.Py.list
import io.particle.android.sdk.utils.Toaster
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    // private val realm by lazy { Realm.getDefaultInstance() }
    private val receivedEventText by lazy { findViewById(R.id.received_event_text) as TextView }
    private val sendEventButton by lazy { findViewById(R.id.send_event_button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendEventButton.setOnClickListener {
            Observable
                    .fromCallable {
                        App.device.callFunction("emit", list(receivedEventText.text.toString()))
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Toaster.s(this, "yay!")
                    }

        }
    }

    override fun onStart() {
        super.onStart()
        subscribeToParticleEvents()
    }

    override fun onStop() {
        super.onStop()
        unsubscribeFromParticleEvents()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Particle events
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private val particleEventListener by lazy {
        var subscriptionId = 0L
        Observable.create({ emitter: ObservableEmitter<ParticleEvent> ->
            subscriptionId = App.device.subscribeToEvents(
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

    var disposable: Disposable? = null

    private fun subscribeToParticleEvents() {
        disposable = particleEventListener
                .subscribe { event ->
                    receivedEventText.text = event.dataPayload
                }
    }

    private fun unsubscribeFromParticleEvents() {
        disposable?.dispose()
        disposable = null
    }
}
