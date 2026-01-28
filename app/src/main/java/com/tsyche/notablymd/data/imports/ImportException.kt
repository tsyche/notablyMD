package com.tsyche.notablymd.data.imports

class ImportException(val textResId: Int, cause: Throwable) : RuntimeException(cause)
