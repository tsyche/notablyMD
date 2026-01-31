package com.tsyche.notablymd.presentation.activity.main.fragment.migration

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tsyche.notablymd.data.migration.MigrationConfig
import com.tsyche.notablymd.data.migration.MigrationProgress
import com.tsyche.notablymd.data.migration.MigrationResult
import com.tsyche.notablymd.databinding.DialogMigrationUtilityBinding
import com.tsyche.notablymd.presentation.viewmodel.MigrationViewModel
import kotlinx.coroutines.launch

/** Dialog fragment for migrating notes to markdown format */
class MigrationUtilityDialog : DialogFragment() {

    private var _binding: DialogMigrationUtilityBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: MigrationViewModel by viewModels()

    private var onMigrationComplete: ((MigrationResult) -> Unit)? = null

    companion object {
        private const val ARG_TARGET_DIRECTORY = "target_directory"

        fun newInstance(targetDirectory: String): MigrationUtilityDialog {
            return MigrationUtilityDialog().apply {
                arguments = Bundle().apply { putString(ARG_TARGET_DIRECTORY, targetDirectory) }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogMigrationUtilityBinding.inflate(LayoutInflater.from(requireContext()))

        setupUI()
        setupObservers()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Migrate to Markdown")
            .setView(binding.root)
            .setPositiveButton("Start Migration", null)
            .setNegativeButton("Cancel", null)
            .create()
    }

    override fun onResume() {
        super.onResume()

        // Override positive button click to prevent dialog dismissal during migration
        val dialog = dialog as? androidx.appcompat.app.AlertDialog
        dialog?.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            if (!viewModel.isMigrationActive()) {
                startMigration()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        // Set target directory
        val targetDirectory =
            requireArguments().getString(ARG_TARGET_DIRECTORY) ?: getDefaultTargetDirectory()
        binding.targetDirectoryEditText.setText(targetDirectory)

        // Setup configuration options
        binding.includeDeletedCheckbox.isChecked = false
        binding.preserveFormatCheckbox.isChecked = true
        binding.createBackupCheckbox.isChecked = true
        binding.skipExistingCheckbox.isChecked = true
        binding.validateOutputCheckbox.isChecked = true

        // Setup batch size
        binding.batchSizeSeekBar.progress = 50
        binding.batchSizeText.text = "Batch size: 50"

        binding.batchSizeSeekBar.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: android.widget.SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    binding.batchSizeText.text = "Batch size: $progress"
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            }
        )

        // Initially hide progress section
        binding.progressSection.visibility = View.GONE
        binding.resultsSection.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.progress.observe(this) { progress -> updateProgressUI(progress) }

        viewModel.migrationResult.observe(this) { result ->
            if (result != null) {
                showMigrationResult(result)
                onMigrationComplete?.invoke(result)
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Migration Error")
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun startMigration() {
        val config =
            MigrationConfig(
                targetDirectory = binding.targetDirectoryEditText.text.toString(),
                includeDeletedNotes = binding.includeDeletedCheckbox.isChecked,
                preserveOriginalFormat = binding.preserveFormatCheckbox.isChecked,
                createBackup = binding.createBackupCheckbox.isChecked,
                batchSize = binding.batchSizeSeekBar.progress,
                skipExistingFiles = binding.skipExistingCheckbox.isChecked,
                validateOutput = binding.validateOutputCheckbox.isChecked,
            )

        // Show progress section
        binding.configurationSection.visibility = View.GONE
        binding.progressSection.visibility = View.VISIBLE

        // Start migration
        lifecycleScope.launch { viewModel.startMigration(config) }
    }

    private fun updateProgressUI(progress: MigrationProgress?) {
        if (progress == null) return

        // Update progress bar
        binding.progressBar.progress = progress.getProgressPercentage()
        binding.progressText.text = "${progress.processedNotes}/${progress.totalNotes} notes"

        // Update status
        binding.statusText.text =
            when (progress.status) {
                MigrationProgress.MigrationStatus.PREPARING -> "Preparing migration..."
                MigrationProgress.MigrationStatus.IN_PROGRESS ->
                    "Migrating: ${progress.currentNoteTitle ?: ""}"
                MigrationProgress.MigrationStatus.PAUSED -> "Migration paused"
                MigrationProgress.MigrationStatus.COMPLETED -> "Migration completed"
                MigrationProgress.MigrationStatus.FAILED -> "Migration failed"
                MigrationProgress.MigrationStatus.CANCELLED -> "Migration cancelled"
                MigrationProgress.MigrationStatus.NOT_STARTED -> "Not started"
            }

        // Update time remaining
        if (progress.estimatedTimeRemaining > 0) {
            val minutes = progress.estimatedTimeRemaining / 60000
            binding.timeRemainingText.text = "Est. time remaining: ${minutes}min"
        } else {
            binding.timeRemainingText.text = ""
        }

        // Show/hide action buttons based on status
        when (progress.status) {
            MigrationProgress.MigrationStatus.IN_PROGRESS -> {
                binding.pauseButton.visibility = View.VISIBLE
                binding.cancelButton.visibility = View.VISIBLE
                binding.pauseButton.text = "Pause"
            }
            MigrationProgress.MigrationStatus.PAUSED -> {
                binding.pauseButton.visibility = View.VISIBLE
                binding.cancelButton.visibility = View.VISIBLE
                binding.pauseButton.text = "Resume"
            }
            else -> {
                binding.pauseButton.visibility = View.GONE
                binding.cancelButton.visibility = View.GONE
            }
        }

        // Show errors if any
        if (progress.errors.isNotEmpty()) {
            binding.errorCountText.text = "Errors: ${progress.errors.size}"
            binding.errorCountText.visibility = View.VISIBLE
        } else {
            binding.errorCountText.visibility = View.GONE
        }
    }

    private fun showMigrationResult(result: MigrationResult) {
        // Hide progress section, show results
        binding.progressSection.visibility = View.GONE
        binding.resultsSection.visibility = View.VISIBLE

        // Update result information
        binding.resultTitle.text =
            if (result.success) "Migration Successful" else "Migration Failed"
        binding.resultSummary.text = result.getSummary()

        // Update statistics
        binding.successCountText.text = result.successfulMigrations.toString()
        binding.failedCountText.text = result.failedMigrations.toString()
        binding.skippedCountText.text = result.skippedMigrations.toString()
        binding.totalCountText.text = result.totalNotes.toString()

        // Update progress bar to show final result
        binding.resultProgressBar.progress = result.getSuccessRate()

        // Show error details if any
        if (result.errors.isNotEmpty()) {
            binding.errorDetailsText.text =
                result.errors.take(5).joinToString("\n") { "${it.noteTitle}: ${it.errorMessage}" }
            if (result.errors.size > 5) {
                binding.errorDetailsText.append("\n... and ${result.errors.size - 5} more errors")
            }
            binding.errorDetailsSection.visibility = View.VISIBLE
        } else {
            binding.errorDetailsSection.visibility = View.GONE
        }

        // Update dialog buttons
        val dialog = dialog as? androidx.appcompat.app.AlertDialog
        dialog?.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.text = "Close"
        dialog?.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.visibility = View.GONE
    }

    private fun getDefaultTargetDirectory(): String {
        return "/storage/emulated/0/Android/media/com.tsyche.notablymd/markdown"
    }

    fun setOnMigrationComplete(listener: (MigrationResult) -> Unit) {
        onMigrationComplete = listener
    }
}
