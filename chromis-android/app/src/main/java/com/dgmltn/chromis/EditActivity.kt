package com.dgmltn.chromis

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import com.dgmltn.chromis.model.IRCommand
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.Realm


class EditActivity : AppCompatActivity() {

    private val realm by lazy { Realm.getDefaultInstance() }

    private val buttonNameText by lazy { findViewById(R.id.button_name_text) as EditText }
    private val commandText by lazy { findViewById(R.id.command_text) as EditText }
    private val buttonDescriptionText by lazy { findViewById(R.id.button_description_text) as EditText }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        (commandText.compoundDrawables[2] as? Animatable)?.start()

        commandText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val row = realm.where(IRCommand::class.java).equalTo("command", s.toString()).findFirst()
                if (row != null) {
                    buttonNameText.setText(row.name, TextView.BufferType.EDITABLE)
                    buttonDescriptionText.setText(row.description, TextView.BufferType.EDITABLE)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onStart() {
        super.onStart()
        subscribeToParticleEvents()
    }

    override fun onStop() {
        super.onStop()
        unsubscribeFromParticleEvents()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                save()
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun save() {
        var row = realm.where(IRCommand::class.java).equalTo("command", commandText.text.toString()).findFirst()
        if (row == null) {
            val nextId = (realm.where(IRCommand::class.java)?.max("id")?.toLong() ?: -1) + 1
            realm.executeTransaction {
                row = realm.createObject(IRCommand::class.java, nextId)
            }
        }

        realm.executeTransaction {
            row.name = buttonNameText.text.toString()
            row.command = commandText.text.toString()
            row.description = buttonDescriptionText.text.toString()
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Particle events
    ///////////////////////////////////////////////////////////////////////////////////////////////

    var disposable: Disposable? = null

    private fun subscribeToParticleEvents() {
        disposable = App.particleEventSubject.observeOn(AndroidSchedulers.mainThread()).subscribe { event ->
            commandText.setText(event.dataPayload, TextView.BufferType.EDITABLE)
        }
    }

    private fun unsubscribeFromParticleEvents() {
        disposable?.dispose()
        disposable = null
    }
}
