package com.tsyche.notablymd.utils.changehistory

import com.tsyche.notablymd.presentation.view.note.listitem.ListManager
import com.tsyche.notablymd.presentation.view.note.listitem.ListState

class ListDeleteChange(old: ListState, new: ListState, listManager: ListManager) :
    ListBatchChange(old, new, listManager)
