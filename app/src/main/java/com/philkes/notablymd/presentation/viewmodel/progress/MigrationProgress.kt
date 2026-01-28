package com.philkes.notablymd.presentation.viewmodel.progress

import com.philkes.notablymd.R
import com.philkes.notablymd.presentation.view.misc.Progress

/**
 * Simple progress model for startup data migrations. We use only the title and inProgress flags
 * with an indeterminate progress bar.
 */
open class MigrationProgress(
    titleId: Int = R.string.migrating_data,
    current: Int = 0,
    total: Int = 0,
    inProgress: Boolean = true,
    indeterminate: Boolean = true,
) : Progress(titleId, current, total, inProgress, indeterminate)
