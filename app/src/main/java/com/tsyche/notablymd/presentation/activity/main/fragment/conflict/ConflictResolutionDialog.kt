package com.tsyche.notablymd.presentation.activity.main.fragment.conflict

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tsyche.notablymd.R
import com.tsyche.notablymd.data.sync.ConflictResolution
import com.tsyche.notablymd.data.sync.ConflictResolutionResult
import com.tsyche.notablymd.data.sync.SyncConflict
import com.tsyche.notablymd.databinding.DialogConflictResolutionBinding
import com.tsyche.notablymd.presentation.viewmodel.ConflictResolutionViewModel

/** Dialog fragment for resolving sync conflicts */
class ConflictResolutionDialog : DialogFragment() {

    private var _binding: DialogConflictResolutionBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: ConflictResolutionViewModel by viewModels()

    private lateinit var conflict: SyncConflict
    private var onResolutionComplete: ((ConflictResolutionResult) -> Unit)? = null

    companion object {
        private const val ARG_CONFLICT = "conflict"

        fun newInstance(conflict: SyncConflict): ConflictResolutionDialog {
            return ConflictResolutionDialog().apply {
                arguments = Bundle().apply { putParcelable(ARG_CONFLICT, conflict) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conflict = requireArguments().getParcelable(ARG_CONFLICT)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogConflictResolutionBinding.inflate(LayoutInflater.from(requireContext()))

        setupUI()
        setupObservers()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Resolve Sync Conflict")
            .setView(binding.root)
            .setPositiveButton("Resolve") { _, _ -> applyResolution() }
            .setNegativeButton("Cancel", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        // Display conflict information
        binding.conflictTitle.text = conflict.noteTitle
        binding.conflictDescription.text = conflict.getDescription()
        binding.conflictType.text = "Type: ${conflict.conflictType.name}"

        // Set severity indicator
        val severityColor =
            when (conflict.getSeverity()) {
                SyncConflict.ConflictSeverity.LOW -> R.color.conflict_low
                SyncConflict.ConflictSeverity.MEDIUM -> R.color.conflict_medium
                SyncConflict.ConflictSeverity.HIGH -> R.color.conflict_high
                SyncConflict.ConflictSeverity.CRITICAL -> R.color.conflict_critical
            }
        binding.severityIndicator.setBackgroundColor(
            ContextCompat.getColor(requireContext(), severityColor)
        )

        // Setup resolution options
        val resolutionOptions = getResolutionOptions()
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resolutionOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.resolutionSpinner.adapter = adapter

        // Setup version comparison
        setupVersionComparison()

        // Show/hide manual merge editor based on selection
        binding.resolutionSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    val selectedAction = resolutionOptions[position].second
                    binding.manualMergeEditor.visibility =
                        if (selectedAction == ConflictResolution.ResolutionAction.MERGE_MANUAL) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
    }

    private fun setupObservers() {
        viewModel.resolutionResult.observe(this) { result ->
            if (result != null) {
                onResolutionComplete?.invoke(result)
                dismiss()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.resolveButton.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Resolution Failed")
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun setupVersionComparison() {
        // Local version
        binding.localVersionTitle.text = "Local Version (${conflict.localVersion.source})"
        binding.localVersionContent.text = conflict.localVersion.content
        binding.localVersionModified.text =
            "Modified: ${java.util.Date(conflict.localVersion.lastModified)}"
        binding.localVersionSize.text = "Size: ${conflict.localVersion.size} chars"

        // Remote version
        binding.remoteVersionTitle.text = "Remote Version (${conflict.remoteVersion.source})"
        binding.remoteVersionContent.text = conflict.remoteVersion.content
        binding.remoteVersionModified.text =
            "Modified: ${java.util.Date(conflict.remoteVersion.lastModified)}"
        binding.remoteVersionSize.text = "Size: ${conflict.remoteVersion.size} chars"
    }

    private fun getResolutionOptions(): List<Pair<String, ConflictResolution.ResolutionAction>> {
        return listOf(
            "Keep Local Version" to ConflictResolution.ResolutionAction.KEEP_LOCAL,
            "Keep Remote Version" to ConflictResolution.ResolutionAction.KEEP_REMOTE,
            "Manual Merge" to ConflictResolution.ResolutionAction.MERGE_MANUAL,
            "Keep Both Versions" to ConflictResolution.ResolutionAction.KEEP_BOTH,
            "Delete Note" to ConflictResolution.ResolutionAction.DELETE_NOTE,
            "Auto-Merge" to ConflictResolution.ResolutionAction.AUTO_MERGE,
        )
    }

    private fun applyResolution() {
        val selectedPosition = binding.resolutionSpinner.selectedItemPosition
        val resolutionOptions = getResolutionOptions()
        val selectedAction = resolutionOptions[selectedPosition].second

        val customContent =
            if (selectedAction == ConflictResolution.ResolutionAction.MERGE_MANUAL) {
                binding.manualMergeEditor.text.toString()
            } else {
                null
            }

        val resolution =
            ConflictResolution(
                conflictId = conflict.noteId,
                action = selectedAction,
                customContent = customContent,
                preserveMetadata = binding.preserveMetadataCheckbox.isChecked,
            )

        viewModel.resolveConflict(conflict, resolution)
    }

    fun setOnResolutionComplete(listener: (ConflictResolutionResult) -> Unit) {
        onResolutionComplete = listener
    }
}
