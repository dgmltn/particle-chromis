package com.dgmltn.chromis

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView
import com.dgmltn.chromis.model.IRCommand
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.RealmBasedRecyclerViewAdapter
import io.realm.RealmResults
import io.realm.RealmViewHolder
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private val realm by lazy { Realm.getDefaultInstance() }
    private val coordinator by lazy { findViewById(R.id.coordinator) }
    private val recycler by lazy { findViewById(R.id.recycler) as RealmRecyclerView }

    private val commands by lazy { realm.where(IRCommand::class.java).findAll() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = IRCommandAdapter(this, commands, true, true, null)
        recycler.setAdapter(adapter)
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
                startActivity(EditActivity.getIntent(this))
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

            val snackbar = Snackbar.make(coordinator, row?.name ?: command, Snackbar.LENGTH_LONG)
            if (row == null) {
                snackbar.setAction(R.string.Add, { startActivity(EditActivity.getIntent(this, command)) })
            }
            else {
                snackbar.setAction(R.string.Edit, { startActivity(EditActivity.getIntent(this, command)) })
            }
            snackbar.show()
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
    ), View.OnClickListener, View.OnLongClickListener {

        inner class ViewHolder(container: ViewGroup) : RealmViewHolder(container) {
            var name = container.findViewById(R.id.name) as TextView
        }

        override fun onCreateRealmViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
            val v = inflater.inflate(R.layout.row_ircommand, viewGroup, false)
            return ViewHolder(v as ViewGroup)
        }

        override fun onBindRealmViewHolder(realmViewHolder: ViewHolder, position: Int) {
            realmViewHolder.name.text = commands[position].name
            realmViewHolder.itemView.tag = commands[position].command
            realmViewHolder.itemView.setOnClickListener(this)
            realmViewHolder.itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            val command = view.tag
            App.particleFunctionCall("emit", command)
                    .subscribe {
                        if (it == -1) {
                            Snackbar.make(coordinator, R.string.snackbar_failed, Snackbar.LENGTH_LONG).show()
                        }
                        else {
                            Snackbar.make(coordinator, R.string.snackbar_sent, Snackbar.LENGTH_LONG).show()
                        }
                    }
        }

        override fun onLongClick(v: View): Boolean {
            startActivity(EditActivity.getIntent(this@MainActivity, v.tag as String?))
            return true
        }
    }

}
