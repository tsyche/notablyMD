package com.philkes.notablymd.presentation.view.main.sorting

import androidx.recyclerview.widget.RecyclerView
import com.philkes.notablymd.data.model.BaseNote
import com.philkes.notablymd.presentation.viewmodel.preference.SortDirection

class BaseNoteModifiedDateSort(adapter: RecyclerView.Adapter<*>?, sortDirection: SortDirection) :
    ItemSort(adapter, sortDirection) {

    override fun compare(note1: BaseNote, note2: BaseNote, sortDirection: SortDirection): Int {
        val sort = note1.compareModified(note2)
        return if (sortDirection == SortDirection.ASC) sort else -1 * sort
    }
}

fun BaseNote.compareModified(other: BaseNote) = modifiedTimestamp.compareTo(other.modifiedTimestamp)
