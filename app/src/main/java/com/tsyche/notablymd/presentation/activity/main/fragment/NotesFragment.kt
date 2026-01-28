package com.tsyche.notablymd.presentation.activity.main.fragment

import android.os.Bundle
import android.view.View
import com.tsyche.notablymd.R
import com.tsyche.notablymd.data.model.Folder

class NotesFragment : NotablyMDFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.NOTES
    }

    override fun getObservable() = model.baseNotes!!

    override fun getBackground() = R.drawable.notebook
}
