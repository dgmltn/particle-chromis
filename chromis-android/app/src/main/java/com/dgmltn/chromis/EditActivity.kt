package com.dgmltn.chromis

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.dgmltn.chromis.model.IRCommand
import io.reactivex.disposables.Disposable
import io.realm.Realm
import java.util.*
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus


class EditActivity : AppCompatActivity() {

    private val realm by lazy { Realm.getDefaultInstance() }

    private val buttonNameText by lazy { findViewById(R.id.button_name_text) as EditText }
    private val commandText by lazy { findViewById(R.id.command_text) as EditText }
    private val buttonDescriptionText by lazy { findViewById(R.id.button_description_text) as EditText }
    private val saveButton by lazy { findViewById(R.id.save_button) }
    private val cancelButton by lazy { findViewById(R.id.cancel_button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

//        buttonNameText.setText(persisted.name, TextView.BufferType.EDITABLE)
//        commandText.setText(persisted.command, TextView.BufferType.EDITABLE)
//        buttonDescriptionText.setText(persisted.description, TextView.BufferType.EDITABLE)


        val image = findViewById(R.id.anim_test) as ImageView
        val drawable = image.drawable
        if (drawable is Animatable) {
            drawable.start()
        }

        saveButton.setOnClickListener {
            save()
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
        disposable = App.particleEventListener.subscribe { event ->
            commandText.setText(event.dataPayload, TextView.BufferType.EDITABLE)
        }
    }

    private fun unsubscribeFromParticleEvents() {
        disposable?.dispose()
        disposable = null
    }
}
