package com.dgmltn.chromis

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.dgmltn.chromis.model.IRCommand
import io.particle.android.sdk.utils.Toaster
import io.reactivex.disposables.Disposable
import io.realm.Realm
import java.util.*


class EditActivity : AppCompatActivity() {

    private val realm by lazy { Realm.getDefaultInstance() }
    private val buttonNameText by lazy { findViewById(R.id.button_name_text) as EditText }
    private val receivedEventText by lazy { findViewById(R.id.received_event_text) as EditText }
    private val buttonDescriptionText by lazy { findViewById(R.id.button_description_text) as EditText }
    private val saveButton by lazy { findViewById(R.id.save_button) }
    private val cancelButton by lazy { findViewById(R.id.cancel_button) }

    private val persisted: IRCommand
        get() {
            var ret = realm.where(IRCommand::class.java).findFirst()
            if (ret == null) {
                realm.executeTransaction {
                    ret = realm.createObject(IRCommand::class.java, UUID.randomUUID())
                }
            }
            return ret
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        buttonNameText.setText(persisted.name, TextView.BufferType.EDITABLE)
        receivedEventText.setText(persisted.command, TextView.BufferType.EDITABLE)
        buttonDescriptionText.setText(persisted.description, TextView.BufferType.EDITABLE)

        saveButton.setOnClickListener {
            realm.executeTransaction {
                persisted.name = buttonNameText.text.toString()
                persisted.command = receivedEventText.text.toString()
                persisted.description = buttonDescriptionText.text.toString()
            }
            finish()
        }

        cancelButton.setOnClickListener {
            finish()
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
        disposable = App.particleEventListener.subscribe { event ->
            receivedEventText.setText(event.dataPayload, TextView.BufferType.EDITABLE)
            realm.executeTransaction {
                persisted.command = event.dataPayload
            }
        }
    }

    private fun unsubscribeFromParticleEvents() {
        disposable?.dispose()
        disposable = null
    }
}
