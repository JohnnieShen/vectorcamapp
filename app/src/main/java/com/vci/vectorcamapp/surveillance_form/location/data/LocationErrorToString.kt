package com.vci.vectorcamapp.surveillance_form.location.data

import android.content.Context
import com.vci.vectorcamapp.R

fun LocationError.toString(context: Context): String {
    val resId = when(this) {
        LocationError.PERMISSION_DENIED -> R.string.location_error_permission_denied
        LocationError.GPS_TIMEOUT -> R.string.location_error_gps_timeout
        LocationError.UNKNOWN -> R.string.location_error_unknown
    }
    return context.getString(resId)
}
