package com.philkes.notablymd.data.model

import kotlinx.parcelize.Parcelize

@Parcelize
data class Audio(var name: String, var duration: Long?, var timestamp: Long) : Attachment
