package com.philkes.notablymd.presentation.viewmodel.progress

import com.philkes.notablymd.R
import com.philkes.notablymd.presentation.view.misc.Progress

open class DeleteProgress(
    current: Int = 0,
    total: Int = 0,
    inProgress: Boolean = true,
    indeterminate: Boolean = false,
) : Progress(R.string.deleting_files, current, total, inProgress, indeterminate)
