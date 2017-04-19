package com.dgmltn.chromis

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView
import com.dgmltn.chromis.model.IRCommand
import io.particle.android.sdk.utils.Toaster
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.RealmBasedRecyclerViewAdapter
import io.realm.RealmResults
import io.realm.RealmViewHolder
import timber.log.Timber
import io.reactivex.android.schedulers.AndroidSchedulers


class MainActivity : AppCompatActivity() {

    private val realm by lazy { Realm.getDefaultInstance() }
    private val container by lazy { findViewById(R.id.container) }
    private val recycler by lazy { findViewById(R.id.recycler) as RealmRecyclerView }

    private val commands by lazy { realm.where(IRCommand::class.java).findAll() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = IRCommandAdapter(this, commands, true, true, null)
        recycler.setAdapter(adapter)

//        sendEventButton.setOnClickListener {
//            App.particleFunctionCall("emit", receivedEventText.text)
//                    .subscribe {
//                        Toaster.s(this, "yay!")
//                    }
//        }
        Snackbar.make(container, "SNACKBAR!!", Snackbar.LENGTH_LONG).show()
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
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                startActivity(Intent(this, EditActivity::class.java))
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
        disposable = App.particleEventSubject.observeOn(AndroidSchedulers.mainThread()).subscribe {
            Timber.i("Got event %s", it.dataPayload)
            val command = it.dataPayload
            val row = realm.where(IRCommand::class.java).equalTo("command", command).findFirst()
            Snackbar.make(container, row?.name ?: command, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun unsubscribeFromParticleEvents() {
        disposable?.dispose()
        disposable = null
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // RecyclerView
    ///////////////////////////////////////////////////////////////////////////////////////////////

    inner class IRCommandAdapter(
            context: Context,
            realmResults: RealmResults<IRCommand>,
            automaticUpdate: Boolean,
            animateResults: Boolean,
            animateExtraColumnName: String?)
        : RealmBasedRecyclerViewAdapter<IRCommand, IRCommandAdapter.ViewHolder>(
            context,
            realmResults,
            automaticUpdate,
            animateResults,
            animateExtraColumnName
    ) {

        inner class ViewHolder(container: ViewGroup) : RealmViewHolder(container) {
            var name = container.findViewById(R.id.name) as TextView
            var command = container.findViewById(R.id.command) as TextView
            var description = container.findViewById(R.id.description) as TextView
        }

        override fun onCreateRealmViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
            val v = inflater.inflate(R.layout.row_ircommand, viewGroup, false)
            return ViewHolder(v as ViewGroup)
        }

        override fun onBindRealmViewHolder(realmViewHolder: ViewHolder, position: Int) {
            realmViewHolder.name.text = commands[position].name
            realmViewHolder.command.text = commands[position].command
            realmViewHolder.description.text = commands[position].description
            realmViewHolder.itemView.setOnClickListener {
            App.particleFunctionCall("emit", commands[position].command)
                    .subscribe {
                        Toaster.s(context as Activity, "yay!")
                    }
            }
        }
    }

}
