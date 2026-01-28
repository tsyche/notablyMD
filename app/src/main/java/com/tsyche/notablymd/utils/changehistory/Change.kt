package com.tsyche.notablymd.utils.changehistory

interface Change {
    fun redo()

    fun undo()
}
