package com.tsyche.notablymd.utils

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tsyche.notablymd.R
import com.tsyche.notablymd.R.string.auto_backup_failed
import com.tsyche.notablymd.R.string.crash_export_backup_failed
import com.tsyche.notablymd.R.string.report_bug
import com.tsyche.notablymd.databinding.ActivityErrorBinding
import com.tsyche.notablymd.presentation.getQuantityString
import com.tsyche.notablymd.presentation.setCancelButton
import com.tsyche.notablymd.presentation.setupProgressDialog
import com.tsyche.notablymd.presentation.showToast
import com.tsyche.notablymd.presentation.view.misc.Progress
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences
import com.tsyche.notablymd.utils.backup.BACKUP_TIMESTAMP_FORMATTER
import com.tsyche.notablymd.utils.backup.exportAsZip
import java.util.Date
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity used when the app is about to crash. Implicitly used by
 * `cat.ereza:customactivityoncrash`.
 */
class ErrorActivity : AppCompatActivity() {

    private lateinit var exportBackupActivityResultLauncher: ActivityResultLauncher<Intent>
    private val exportBackupProgress = MutableLiveData<Progress>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            RestartButton.setOnClickListener {
                CustomActivityOnCrash.restartApplication(
                    this@ErrorActivity,
                    CustomActivityOnCrash.getConfigFromIntent(intent)!!,
                )
            }
            val stacktrace = CustomActivityOnCrash.getStackTraceFromIntent(intent)
            stacktrace?.let {
                application.log(TAG, stackTrace = it)
                ExceptionTitle.text = stacktrace.lines().firstOrNull()?.replaceFirst(":", ":\n")
                ExceptionDetails.text = stacktrace.lines().drop(1).joinToString("\n")
                CopyButton.setOnClickListener { copyToClipBoard(stacktrace) }
            }
            ReportButton.setOnClickListener { reportBug(stacktrace) }
            ViewLogsButton.setOnClickListener { viewLogs() }
            setupExportBackup(binding, stacktrace)
        }
    }

    private fun setupExportBackup(binding: ActivityErrorBinding, stacktrace: String?) {
        binding.ExportBackupButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setMessage(
                    getString(
                        R.string.crash_export_backup_message,
                        getString(R.string.continue_),
                        getString(R.string.report_bug),
                    )
                )
                .setPositiveButton(R.string.continue_) { _, _ ->
                    val intent =
                        Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .apply {
                                type = MIME_TYPE_ZIP
                                addCategory(Intent.CATEGORY_OPENABLE)
                                putExtra(
                                    Intent.EXTRA_TITLE,
                                    "NotablyMD_Crash_Backup-${BACKUP_TIMESTAMP_FORMATTER.format(Date())}",
                                )
                            }
                            .wrapWithChooser(this@ErrorActivity)
                    exportBackupActivityResultLauncher.launch(intent)
                }
                .setCancelButton()
                .show()
        }
        exportBackupActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        val preferences = NotablyMDPreferences.getInstance(this)
                        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                            showErrorDialog(
                                throwable,
                                auto_backup_failed,
                                getString(crash_export_backup_failed, this.getString(report_bug)),
                                originalStacktrace = stacktrace,
                            )
                        }
                        lifecycleScope.launch(exceptionHandler) {
                            val exportedNotes =
                                withContext(Dispatchers.IO) {
                                    return@withContext application.exportAsZip(
                                        uri,
                                        password = preferences.backupPassword.value,
                                        backupProgress = exportBackupProgress,
                                    )
                                }
                            val message =
                                application.getQuantityString(
                                    R.plurals.exported_notes,
                                    exportedNotes,
                                )
                            application.showToast(message)
                        }
                    }
                }
            }
        exportBackupProgress.setupProgressDialog(this)
    }

    companion object {
        private const val TAG = "ErrorActivity"
    }
}
