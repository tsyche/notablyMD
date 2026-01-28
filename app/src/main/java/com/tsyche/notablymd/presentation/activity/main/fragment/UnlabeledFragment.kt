package com.tsyche.notablymd.presentation.activity.main.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import com.tsyche.notablymd.R
import com.tsyche.notablymd.data.model.Folder
import com.tsyche.notablymd.data.model.Item

class UnlabeledFragment : NotablyMDFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.folder.value = Folder.NOTES
    }

    override fun getBackground() = R.drawable.label_off

    override fun getObservable(): LiveData<List<Item>> {
        return model.getNotesWithoutLabel()
    }
}
