package com.tsyche.notablymd.utils.changehistory

import com.tsyche.notablymd.presentation.view.note.listitem.ListManager
import com.tsyche.notablymd.presentation.view.note.listitem.ListState

open class ListBatchChange(old: ListState, new: ListState, private val listManager: ListManager) :
    ValueChange<ListState>(old, new) {

    override fun update(value: ListState, isUndo: Boolean) {
        listManager.setState(if (isUndo) oldValue else newValue)
    }

    override fun toString(): String {
        return javaClass.simpleName
    }
}
