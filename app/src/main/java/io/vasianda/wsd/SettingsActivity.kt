package io.vasianda.wsd

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            findPreference<EditTextPreference>("password")?.let { pwdPref ->
                pwdPref.summaryProvider = Preference.SummaryProvider<Preference?> {
                    val password: String = PreferenceManager.getDefaultSharedPreferences(context).getString("password", "")!!
                    if (password.isEmpty()) password else asterisks(password.length)
                }

                pwdPref.setOnBindEditTextListener { editText ->
                    editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    pwdPref.summaryProvider = Preference.SummaryProvider<Preference> { asterisks(editText.text.toString().length) }
                }

            }
        }
        private fun asterisks(length: Int): String = String(CharArray(length).apply { fill('*') })
    }
}