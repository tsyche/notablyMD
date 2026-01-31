package com.tsyche.notablymd.presentation.activity

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.tsyche.notablymd.R
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences

/**
 * Voice Widget Configuration Activity
 *
 * Allows users to configure the voice widget before adding it to the home screen. Currently
 * provides basic configuration options that can be expanded later.
 */
class VoiceWidgetConfigureActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var preferences: NotablyMDPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_widget_configure)

        preferences = NotablyMDPreferences.getInstance(this)

        // Find the widget id from the intent
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId =
                extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID,
                )
        }

        // If they gave us an intent without the widget id, just do nothing
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        val titleText = findViewById<TextView>(R.id.titleText)
        val descriptionText = findViewById<TextView>(R.id.descriptionText)
        val addButton = findViewById<Button>(R.id.addButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        titleText.text = getString(R.string.voice_note_widget)
        descriptionText.text = getString(R.string.voice_widget_configure_description)

        addButton.setOnClickListener {
            // Save any configuration settings
            saveConfiguration()

            // Return widget ID to the widget host
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }

        cancelButton.setOnClickListener {
            // Cancel widget creation
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun saveConfiguration() {
        // Save widget-specific preferences
        // For now, we'll just mark that the widget has been configured
        val prefs = getSharedPreferences("voice_widget_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("voice_widget_${appWidgetId}_configured", true).apply()
    }

    companion object {
        private const val PREFS_NAME = "com.tsyche.notablymd.widget.VoiceWidget"
    }
}
