package com.philkes.notablymd.presentation.view.note.listitem.sorting

import androidx.recyclerview.widget.SortedList
import com.philkes.notablymd.data.model.ListItem

class SortedItemsList(val callback: ListItemParentSortCallback) :
    SortedList<ListItem>(ListItem::class.java, callback) {

    init {
        this.callback.setItems(this)
    }
}
