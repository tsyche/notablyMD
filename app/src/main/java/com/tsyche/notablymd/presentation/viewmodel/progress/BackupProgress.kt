package com.tsyche.notablymd.presentation.viewmodel.progress

import com.tsyche.notablymd.R
import com.tsyche.notablymd.presentation.view.misc.Progress

open class BackupProgress(
    current: Int = 0,
    total: Int = 0,
    inProgress: Boolean = true,
    indeterminate: Boolean = false,
) : Progress(R.string.export_backup, current, total, inProgress, indeterminate)
