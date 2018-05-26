package org.gdprcmp

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import org.gdprcmplib.CmpActivityResult
import org.gdprcmplib.GdprCmp
import android.content.Context.CLIPBOARD_SERVICE
import android.content.ClipData
import android.widget.Button
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    //val subjectToGdpr = findViewById(R.id.subject_to_gdpr) as TextView

    val REQ_CODE = 10;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateUI()

    }

    fun updateUI() {
        if (GdprCmp.isSubjectToGDPR(this)) {
            findViewById<TextView>(R.id.subject_to_gdpr).setText(R.string.is_subject_to_gdpr);
            findViewById<ToggleButton>(R.id.toggle_gdpr).setChecked(true)
        } else {
            findViewById<TextView>(R.id.subject_to_gdpr).setText(R.string.is_not_subject_to_gdpr);
            findViewById<ToggleButton>(R.id.toggle_gdpr).setChecked(false)
        }
        if (GdprCmp.hasGDPRConsentString(this)) {
            findViewById<Button>(R.id.copy_to_clipboard).visibility = View.VISIBLE
            findViewById<TextView>(R.id.consent_string).setText(""+GdprCmp.getGDPRConsentString(this))
        } else {
            findViewById<Button>(R.id.copy_to_clipboard).visibility = View.GONE
            findViewById<TextView>(R.id.consent_string).setText(R.string.consent_string_not_set)
        }
    }

    fun onPrivacySettings(view: View?) {
        GdprCmp.startCmpActivityForResult(this, REQ_CODE, true)
    }

    fun onToggleGDPR(view: View?) {
        GdprCmp.setIsSubjectToGDPR(this,findViewById<ToggleButton>(R.id.toggle_gdpr).isChecked())
        updateUI()
    }

    fun onCopyToClipboard(view: View?) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // Creates a new text clip to put on the clipboard
        val clip = ClipData.newPlainText("simple text", ""+findViewById<TextView>(R.id.consent_string).getText())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE) {
            if (resultCode == CmpActivityResult.RESULT_CONSENT_ALL) {
                Log.i("MainActivity","CmpActivityResult.RESULT_CONSENT_ALL")
            } else if (resultCode == CmpActivityResult.RESULT_CONSENT_CUSTOM_PARTIAL) {
                Log.i("MainActivity","CmpActivityResult.RESULT_CONSENT_CUSTOM_PARTIAL")
            } else if (resultCode == CmpActivityResult.RESULT_CONSENT_NONE) {
                Log.i("MainActivity","CmpActivityResult.RESULT_CONSENT_NONE")
            } else if (resultCode == CmpActivityResult.RESULT_CANCELED_CONSENT) {
                Log.i("MainActivity","CmpActivityResult.RESULT_CANCELED_CONSENT")
            } else if (resultCode == CmpActivityResult.RESULT_COULD_NOT_FETCH_VENDOR_LIST) {
                Log.i("MainActivity","CmpActivityResult.RESULT_COULD_NOT_FETCH_VENDOR_LIST")
            }
            updateUI()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
