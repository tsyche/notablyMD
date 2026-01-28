package com.philkes.notablymd.utils.changehistory

import com.philkes.notablymd.presentation.view.note.listitem.ListManager
import com.philkes.notablymd.presentation.view.note.listitem.ListState

class ListMoveChange(old: ListState, new: ListState, listManager: ListManager) :
    ListBatchChange(old, new, listManager)
