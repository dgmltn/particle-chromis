package com.dgmltn.chromis

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.dgmltn.chromis.model.IRCommand
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.Realm
import pl.coreorb.selectiondialogs.data.SelectableIcon
import pl.coreorb.selectiondialogs.dialogs.IconSelectDialog


class EditActivity : AppCompatActivity(), IconSelectDialog.OnIconSelectedListener {

    companion object {
        val TAG_SELECT_ICON_DIALOG = "TAG_SELECT_ICON_DIALOG"

        val ARG_COMMAND = "ARG_COMMAND"

        fun getIntent(context: Context, command: String? = null): Intent {
            val intent = Intent(context, EditActivity::class.java)
            if (command != null) {
                intent.putExtra(ARG_COMMAND, command)
            }
            return intent
        }
    }

    private val realm by lazy { Realm.getDefaultInstance() }

    private val buttonNameText by lazy { findViewById(R.id.button_name_text) as EditText }
    private val commandText by lazy { findViewById(R.id.command_text) as EditText }
    private val buttonIcon by lazy { findViewById(R.id.icon) as ImageView }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            load(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        (commandText.compoundDrawables[2] as Animatable).start()

        commandText.addTextChangedListener(textWatcher)

        val argCommand = intent?.extras?.getString(ARG_COMMAND)
        if (argCommand != null) {
            commandText.setText(argCommand, TextView.BufferType.EDITABLE)
        }

        buttonIcon.setOnClickListener {
            showIconSelectDialog()
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Database
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private fun load(command: String) {
        val row = realm.where(IRCommand::class.java).equalTo("command", command).findFirst()
        if (row != null) {
            buttonNameText.setText(row.name, TextView.BufferType.EDITABLE)
            iconId = row.icon
        }
    }

    private fun save() {
        val command = commandText.text.toString()
        val name = buttonNameText.text.toString()

        var row = realm.where(IRCommand::class.java).equalTo("command", command).findFirst()
        if (row == null) {
            val nextId = (realm.where(IRCommand::class.java)?.max("id")?.toLong() ?: -1) + 1
            realm.executeTransaction {
                row = realm.createObject(IRCommand::class.java, nextId)
            }
        }

        realm.executeTransaction {
            row.command = command
            row.name = name
            row.icon = iconId
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Icon
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private var iconId: String = ""
        set(value) {
            field = value
            buttonIcon.setImageResource(findIconFor(value))
        }

    private fun showIconSelectDialog() {
        IconSelectDialog.Builder(this)
                .setIcons((application as App).icons)
                .setTitle(R.string.Button_Icon)
                .setSortIconsByName(true)
                .setOnIconSelectedListener(this)
                .build()
                .show(supportFragmentManager, TAG_SELECT_ICON_DIALOG)
    }

    override fun onIconSelected(selectedItem: SelectableIcon) {
        iconId = selectedItem.id
    }

    private fun findIconFor(id: String) =
        (application as App).icons.find { it.id == id }?.drawableResId ?: R.drawable.ic_button_help

}
