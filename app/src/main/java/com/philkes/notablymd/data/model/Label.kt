package com.philkes.notablymd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity class Label(@PrimaryKey val value: String)
