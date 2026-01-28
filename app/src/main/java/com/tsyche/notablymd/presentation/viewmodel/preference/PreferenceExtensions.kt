package com.tsyche.notablymd.presentation.viewmodel.preference

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/** Extension for NotablyMDPreferences to add markdown sync settings */
fun NotablyMDPreferences.markdownSyncEnabled(): LiveData<Boolean> {
    // For now, return a default value
    // In a real implementation, this would be stored in SharedPreferences
    return MutableLiveData(true)
}
