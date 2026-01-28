package com.philkes.notablymd.presentation.activity.main.fragment

import android.os.Bundle
import android.view.View
import com.philkes.notablymd.R
import com.philkes.notablymd.data.model.Folder

class NotesFragment : NotablyMDFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.NOTES
    }

    override fun getObservable() = model.baseNotes!!

    override fun getBackground() = R.drawable.notebook
}
