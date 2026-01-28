package com.tsyche.notablymd

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.color.DynamicColors
import com.tsyche.notablymd.presentation.setEnabledSecureFlag
import com.tsyche.notablymd.presentation.view.misc.NotNullLiveData
import com.tsyche.notablymd.presentation.viewmodel.preference.BiometricLock
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences.Companion.EMPTY_PATH
import com.tsyche.notablymd.presentation.viewmodel.preference.Theme
import com.tsyche.notablymd.presentation.widget.WidgetProvider
import com.tsyche.notablymd.utils.backup.AUTO_BACKUP_WORK_NAME
import com.tsyche.notablymd.utils.backup.autoBackupOnSave
import com.tsyche.notablymd.utils.backup.autoBackupOnSaveFileExists
import com.tsyche.notablymd.utils.backup.cancelAutoBackup
import com.tsyche.notablymd.utils.backup.containsNonCancelled
import com.tsyche.notablymd.utils.backup.createBackup
import com.tsyche.notablymd.utils.backup.deleteModifiedNoteBackup
import com.tsyche.notablymd.utils.backup.isEqualTo
import com.tsyche.notablymd.utils.backup.modifiedNoteBackupExists
import com.tsyche.notablymd.utils.backup.scheduleAutoBackup
import com.tsyche.notablymd.utils.backup.updateAutoBackup
import com.tsyche.notablymd.utils.observeOnce
import com.tsyche.notablymd.utils.security.UnlockReceiver
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotablyMDApplication : Application(), Application.ActivityLifecycleCallbacks {

    private lateinit var biometricLockObserver: Observer<BiometricLock>
    private lateinit var preferences: NotablyMDPreferences
    private var unlockReceiver: UnlockReceiver? = null

    val locked = NotNullLiveData(true)

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        if (isTestRunner()) return
        preferences = NotablyMDPreferences.getInstance(this)
        if (preferences.useDynamicColors.value) {
            if (DynamicColors.isDynamicColorAvailable()) {
                DynamicColors.applyToActivitiesIfAvailable(this)
            }
        } else {
            setTheme(R.style.AppTheme)
        }
        preferences.theme.observeForeverWithPrevious { (oldTheme, theme) ->
            when (theme) {
                Theme.DARK ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                Theme.LIGHT ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                Theme.FOLLOW_SYSTEM ->
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
            }
            if (oldTheme != null) {
                WidgetProvider.updateWidgets(
                    this,
                    locked = preferences.isLockEnabled && locked.value,
                )
            }
        }

        preferences.backupsFolder.observeForeverWithPrevious { (backupFolderBefore, backupFolder) ->
            checkUpdatePeriodicBackup(
                backupFolderBefore,
                backupFolder,
                preferences.periodicBackups.value.periodInDays.toLong(),
                execute = true,
            )
            checkUpdateAutoBackupOnSave(backupFolderBefore, backupFolder)
        }
        preferences.periodicBackups.observeForever { value ->
            val backupFolder = preferences.backupsFolder.value
            checkUpdatePeriodicBackup(backupFolder, backupFolder, value.periodInDays.toLong())
        }

        val filter = IntentFilter().apply { addAction(Intent.ACTION_SCREEN_OFF) }
        biometricLockObserver = Observer { biometricLock ->
            if (biometricLock == BiometricLock.ENABLED) {
                unlockReceiver = UnlockReceiver(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(unlockReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    registerReceiver(unlockReceiver, filter)
                }
            } else {
                unlockReceiver?.let { unregisterReceiver(it) }
                if (locked.value) {
                    locked.postValue(false)
                }
            }
        }
        preferences.biometricLock.observeForever(biometricLockObserver)

        locked.observeForever { isLocked ->
            WidgetProvider.updateWidgets(this, locked = preferences.isLockEnabled && isLocked)
        }

        preferences.backupPassword.observeForeverWithPrevious {
            (previousBackupPassword, backupPassword) ->
            if (preferences.backupOnSave.value) {
                val backupPath = preferences.backupsFolder.value
                if (backupPath != EMPTY_PATH) {
                    if (
                        !modifiedNoteBackupExists(backupPath) ||
                            (previousBackupPassword != null &&
                                previousBackupPassword != backupPassword)
                    ) {
                        deleteModifiedNoteBackup(backupPath)
                        runOnIODispatcher {
                            autoBackupOnSave(
                                backupPath,
                                savedNote = null,
                                password = backupPassword,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun folderChanged(folderBefore: String?, folderAfter: String): Boolean {
        if (folderBefore == null || folderAfter == EMPTY_PATH) {
            return false
        }
        return folderBefore != folderAfter
    }

    private fun checkUpdatePeriodicBackup(
        backupFolderBefore: String?,
        backupFolder: String,
        periodInDays: Long,
        execute: Boolean = false,
    ) {
        val workManager = getWorkManagerSafe() ?: return
        workManager.getWorkInfosForUniqueWorkLiveData(AUTO_BACKUP_WORK_NAME).observeOnce { workInfos
            ->
            if (backupFolder == EMPTY_PATH || periodInDays < 1) {
                if (workInfos?.containsNonCancelled() == true) {
                    workManager.cancelAutoBackup()
                }
            } else if (
                workInfos.isNullOrEmpty() ||
                    workInfos.all { it.state == WorkInfo.State.CANCELLED } ||
                    folderChanged(backupFolderBefore, backupFolder)
            ) {
                workManager.scheduleAutoBackup(this, periodInDays)
                if (execute) {
                    runOnIODispatcher { createBackup() }
                }
            } else if (
                workInfos.first().periodicityInfo?.isEqualTo(periodInDays, TimeUnit.DAYS) == false
            ) {
                workManager.updateAutoBackup(workInfos, periodInDays)
                if (execute) {
                    runOnIODispatcher { createBackup() }
                }
            }
        }
    }

    private fun checkUpdateAutoBackupOnSave(backupFolderBefore: String?, backupFolder: String) {
        if (preferences.backupOnSave.value) {
            if (
                backupFolderBefore == null &&
                    backupFolder != EMPTY_PATH &&
                    !autoBackupOnSaveFileExists(backupFolder)
            ) {
                runOnIODispatcher {
                    autoBackupOnSave(backupFolder, preferences.backupPassword.value, null)
                }
            }
        } else if (folderChanged(backupFolderBefore, backupFolder)) {
            runOnIODispatcher {
                autoBackupOnSave(backupFolder, preferences.backupPassword.value, null)
            }
        }
    }

    private fun getWorkManagerSafe(): WorkManager? {
        return try {
            WorkManager.getInstance(this)
        } catch (e: Exception) {
            // TODO: Happens when ErrorActivity is launched
            null
        }
    }

    private fun <T> runOnIODispatcher(block: suspend CoroutineScope.() -> T) {
        MainScope().launch { withContext(Dispatchers.IO, block) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.setEnabledSecureFlag(preferences.secureFlag.value)
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        private fun isTestRunner(): Boolean {
            return Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
        }
    }
}
