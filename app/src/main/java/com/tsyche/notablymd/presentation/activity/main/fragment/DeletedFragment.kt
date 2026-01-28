package com.tsyche.notablymd.presentation.activity.main.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tsyche.notablymd.R
import com.tsyche.notablymd.data.model.Folder
import com.tsyche.notablymd.presentation.add
import com.tsyche.notablymd.presentation.setCancelButton

class DeletedFragment : NotablyMDFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.DELETED
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(R.string.delete_all, R.drawable.delete_all) { deleteAllNotes() }
    }

    private fun deleteAllNotes() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_all_notes)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteAllTrashedBaseNotes() }
            .setCancelButton()
            .show()
    }

    override fun getBackground() = R.drawable.delete

    override fun getObservable() = model.deletedNotes!!
}
