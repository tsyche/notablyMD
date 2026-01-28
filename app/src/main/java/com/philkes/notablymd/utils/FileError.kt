package com.philkes.notablymd.utils

import com.philkes.notablymd.presentation.viewmodel.NotallyModel

data class FileError(
    val name: String,
    val description: String,
    val fileType: NotallyModel.FileType,
)
