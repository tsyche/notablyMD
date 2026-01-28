package com.tsyche.notablymd.presentation.view.main.sorting

import androidx.recyclerview.widget.RecyclerView
import com.tsyche.notablymd.data.model.BaseNote
import com.tsyche.notablymd.presentation.viewmodel.preference.SortDirection

class BaseNoteColorSort(adapter: RecyclerView.Adapter<*>?, sortDirection: SortDirection) :
    ItemSort(adapter, sortDirection) {

    override fun compare(note1: BaseNote, note2: BaseNote, sortDirection: SortDirection): Int {
        val sort =
            note1.compareColor(note2).takeIf { it != 0 } ?: return -note1.compareModified(note2)
        return if (sortDirection == SortDirection.ASC) sort else -1 * sort
    }
}

fun BaseNote.compareColor(other: BaseNote) = color.compareTo(other.color)
