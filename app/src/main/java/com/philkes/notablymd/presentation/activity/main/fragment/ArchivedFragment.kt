package com.philkes.notablymd.presentation.activity.main.fragment

import android.os.Bundle
import android.view.View
import com.philkes.notablymd.R
import com.philkes.notablymd.data.model.Folder

class ArchivedFragment : NotablyMDFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.ARCHIVED
    }

    override fun getBackground() = R.drawable.archive

    override fun getObservable() = model.archivedNotes!!
}
