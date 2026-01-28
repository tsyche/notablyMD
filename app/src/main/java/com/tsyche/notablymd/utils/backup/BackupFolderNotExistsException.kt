package com.tsyche.notablymd.utils.backup

class BackupFolderNotExistsException(val path: String, cause: Throwable? = null) :
    IllegalArgumentException("Folder '$path' does not exist", cause)
