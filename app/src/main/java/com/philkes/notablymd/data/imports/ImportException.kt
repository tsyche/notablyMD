package com.philkes.notablymd.data.imports

class ImportException(val textResId: Int, cause: Throwable) : RuntimeException(cause)
