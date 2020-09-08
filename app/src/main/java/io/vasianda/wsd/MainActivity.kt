package io.vasianda.wsd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.mapBoth
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var balanceService: BalanceService

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as WsdApp).applicationGraph.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.reload).setOnClickListener {
            val loadingDialog = AlertDialog.Builder(this).apply { setMessage("Loading...") }.create()
            loadingDialog.show()
            val balance = GlobalScope.async(Dispatchers.IO) {
                val (login, password) = this@MainActivity.loginPassword()
                balanceService.getBalance(login, password)
            }
            GlobalScope.launch(Dispatchers.Main) {
                val balanceResult = withTimeoutOrNull(30_000) { balance.await() } ?: Err("Timeout")
                loadingDialog.dismiss()
                balanceResult.mapBoth({
                    AlertDialog.Builder(this@MainActivity).apply {
                        setMessage("Balance: $it")
                        setPositiveButton("Ok" ) { dialog, _ -> dialog.dismiss() }
                    }.show()
                }) {
                    AlertDialog.Builder(this@MainActivity).apply {
                        setTitle("Error")
                        setMessage(it)
                        setPositiveButton("Ok" ) { dialog, _ -> dialog.dismiss() }
                    }.show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}

fun Context.loginPassword(): Pair<String, CharArray> {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    return prefs.getString("login", "")!! to prefs.getString("password", "")!!.toCharArray()
}
