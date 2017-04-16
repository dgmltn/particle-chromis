package com.dgmltn.chromis

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import io.particle.android.sdk.utils.Toaster
import io.reactivex.disposables.Disposable


class MainActivity : AppCompatActivity() {

    // private val realm by lazy { Realm.getDefaultInstance() }
    private val receivedEventText by lazy { findViewById(R.id.received_event_text) as TextView }
    private val sendEventButton by lazy { findViewById(R.id.send_event_button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendEventButton.setOnClickListener {
            App.particleFunctionCall("emit", receivedEventText.text)
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

    var disposable: Disposable? = null

    private fun subscribeToParticleEvents() {
        disposable = App.particleEventListener.subscribe { receivedEventText.text = it.dataPayload }
    }

    private fun unsubscribeFromParticleEvents() {
        disposable?.dispose()
        disposable = null
    }
}
