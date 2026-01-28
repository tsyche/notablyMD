package com.tsyche.notablymd.utils

import com.tsyche.notablymd.presentation.viewmodel.NotallyModel

data class FileError(
    val name: String,
    val description: String,
    val fileType: NotallyModel.FileType,
)
