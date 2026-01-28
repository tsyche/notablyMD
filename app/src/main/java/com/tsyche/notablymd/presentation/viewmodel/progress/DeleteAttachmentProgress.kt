package com.tsyche.notablymd.presentation.viewmodel.progress

import com.tsyche.notablymd.R
import com.tsyche.notablymd.presentation.view.misc.Progress

open class DeleteAttachmentProgress(
    current: Int = 0,
    total: Int = 0,
    inProgress: Boolean = true,
    indeterminate: Boolean = false,
) : Progress(R.string.deleting_files, current, total, inProgress, indeterminate)
