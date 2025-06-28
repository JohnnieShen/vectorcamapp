package com.vci.vectorcamapp.core.presentation.util.network

import android.content.Context
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.util.network.NetworkError

fun NetworkError.toString(context: Context): String {
    val resId = when(this) {
        NetworkError.NOT_FOUND -> R.string.network_error_not_found
        NetworkError.CONFLICT -> R.string.network_error_conflict
        NetworkError.REQUEST_TIMEOUT -> R.string.network_error_request_timeout
        NetworkError.TOO_MANY_REQUESTS -> R.string.network_error_too_many_requests
        NetworkError.NO_INTERNET -> R.string.network_error_no_internet
        NetworkError.CLIENT_ERROR -> R.string.network_error_client_error
        NetworkError.SERVER_ERROR -> R.string.network_error_server_error
        NetworkError.SERIALIZATION -> R.string.network_error_serialization
        NetworkError.UNKNOWN -> R.string.network_error_unknown

        NetworkError.SESSION_NOT_COMPLETED -> R.string.network_error_session_not_completed
    }
    return context.getString(resId)
}
