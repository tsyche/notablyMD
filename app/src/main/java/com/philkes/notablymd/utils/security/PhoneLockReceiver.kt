package com.philkes.notablymd.utils.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.philkes.notablymd.NotablyMDApplication

class UnlockReceiver(private val application: NotablyMDApplication) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            application.locked.value = true
        }
    }
}
