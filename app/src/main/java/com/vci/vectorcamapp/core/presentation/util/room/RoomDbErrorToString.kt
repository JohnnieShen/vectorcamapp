package com.vci.vectorcamapp.core.presentation.util.room

import android.content.Context
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError

fun RoomDbError.toString(context: Context) : String {
    val resId = when(this) {
        RoomDbError.CONSTRAINT_VIOLATION -> R.string.roomdb_error_constraint_violation
        RoomDbError.UNKNOWN_ERROR -> R.string.roomdb_error_unknown_error
    }
    return context.getString(resId)
}
