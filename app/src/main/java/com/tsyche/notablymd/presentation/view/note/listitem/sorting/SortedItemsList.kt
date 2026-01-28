package com.tsyche.notablymd.presentation.view.note.listitem.sorting

import androidx.recyclerview.widget.SortedList
import com.tsyche.notablymd.data.model.ListItem

class SortedItemsList(val callback: ListItemParentSortCallback) :
    SortedList<ListItem>(ListItem::class.java, callback) {

    init {
        this.callback.setItems(this)
    }
}
