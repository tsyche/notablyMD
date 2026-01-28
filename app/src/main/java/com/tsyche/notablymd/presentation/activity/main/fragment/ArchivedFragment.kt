package com.tsyche.notablymd.presentation.activity.main.fragment

import android.os.Bundle
import android.view.View
import com.tsyche.notablymd.R
import com.tsyche.notablymd.data.model.Folder

class ArchivedFragment : NotablyMDFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.ARCHIVED
    }

    override fun getBackground() = R.drawable.archive

    override fun getObservable() = model.archivedNotes!!
}
