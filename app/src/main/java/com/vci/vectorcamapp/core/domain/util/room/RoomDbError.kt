package com.vci.vectorcamapp.core.domain.util.room

import com.vci.vectorcamapp.core.domain.util.Error

enum class RoomDbError : Error {
    CONSTRAINT_VIOLATION,
    NO_ROWS_AFFECTED,
    UNKNOWN_ERROR
}
