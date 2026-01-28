package com.philkes.notablymd.utils.changehistory

interface Change {
    fun redo()

    fun undo()
}
