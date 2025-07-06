package com.vci.vectorcamapp.core.presentation.util.error

import android.content.Context
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.complete_session.domain.util.CompleteSessionError
import com.vci.vectorcamapp.core.domain.util.Error
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import com.vci.vectorcamapp.incomplete_session.domain.util.IncompleteSessionError
import com.vci.vectorcamapp.main.domain.util.MainError
import com.vci.vectorcamapp.registration.domain.util.RegistrationError
import com.vci.vectorcamapp.surveillance_form.domain.util.SurveillanceFormError
import com.vci.vectorcamapp.surveillance_form.domain.util.FormValidationError

fun Error.toString(context: Context): String {
    val resId = when (this) {
        is NetworkError -> when (this) {
            NetworkError.REQUEST_TIMEOUT -> R.string.network_error_request_timeout
            NetworkError.TOO_MANY_REQUESTS -> R.string.network_error_too_many_requests
            NetworkError.NO_INTERNET -> R.string.network_error_no_internet
            NetworkError.SERVER_ERROR -> R.string.network_error_server_error
            NetworkError.SERIALIZATION_ERROR -> R.string.network_error_serialization_error
            NetworkError.SESSION_NOT_COMPLETED -> R.string.network_error_session_not_completed
            NetworkError.NOT_FOUND -> R.string.network_error_not_found
            NetworkError.CONFLICT -> R.string.network_error_conflict
            NetworkError.CLIENT_ERROR -> R.string.network_error_client_error
            NetworkError.UNKNOWN_ERROR -> R.string.roomdb_error_unknown_error
        }
        is RoomDbError -> when (this) {
            RoomDbError.CONSTRAINT_VIOLATION -> R.string.roomdb_error_constraint_violation
            RoomDbError.UNKNOWN_ERROR -> R.string.roomdb_error_unknown_error
        }
        is CompleteSessionError -> when (this) {
            CompleteSessionError.SESSION_NOT_FOUND -> R.string.complete_session_error_session_not_found
            CompleteSessionError.SESSION_OR_SURVEILLANCE_FORM_NOT_FOUND -> R.string.complete_session_error_session_or_surveillance_form_not_found
            CompleteSessionError.SESSION_OR_SITE_NOT_FOUND -> R.string.complete_session_error_session_or_site_not_found
            CompleteSessionError.UNKNOWN_ERROR -> R.string.complete_session_error_unknown_error
        }
        is ImagingError -> when(this) {
            ImagingError.CAPTURE_ERROR -> R.string.imaging_error_capture_error
            ImagingError.SAVE_ERROR -> R.string.imaging_error_save_error
            ImagingError.PROCESSING_ERROR -> R.string.imaging_error_processing_error
            ImagingError.NO_SPECIMEN_FOUND -> R.string.imaging_error_no_specimen_found
            ImagingError.MULTIPLE_SPECIMENS_FOUND -> R.string.imaging_error_multiple_specimens_found
            ImagingError.NO_ACTIVE_SESSION -> R.string.imaging_error_no_active_session
            ImagingError.MODEL_INITIALIZATION_FAILED -> R.string.imaging_error_model_initialization_failed
            ImagingError.GPU_DELEGATE_INITIALIZATION_FAILED -> R.string.imaging_error_gpu_delegate_initialization_failed
            ImagingError.UNKNOWN_INITIALIZATION_ERROR -> R.string.imaging_error_unknown_initialization_error
            ImagingError.INVALID_INPUT_SHAPE -> R.string.imaging_error_invalid_input_shape
            ImagingError.SPECIMEN_ID_RECOGNITION_FAILED -> R.string.imaging_error_specimen_id_recognition_failed
            ImagingError.SPECIMEN_DETECTION_FAILED -> R.string.imaging_error_specimen_detection_failed
            ImagingError.UNKNOWN_INFERENCE_ERROR -> R.string.imaging_error_unknown_inference_error
            ImagingError.UNKNOWN_ERROR -> R.string.imaging_error_unknown_error
        }
        is IncompleteSessionError -> when (this) {
            IncompleteSessionError.SESSION_NOT_FOUND -> R.string.incomplete_session_error_session_not_found
            IncompleteSessionError.SESSION_RETRIEVAL_FAILED -> R.string.incomplete_session_error_session_retrieval_failed
            IncompleteSessionError.UNKNOWN_ERROR -> R.string.incomplete_session_error_unknown_error
        }
        is MainError -> when (this) {
            MainError.DEVICE_FETCH_FAILED -> R.string.main_error_device_fetch_failed
            MainError.UNKNOWN_ERROR -> R.string.main_error_unknown_error
        }
        is RegistrationError -> when (this) {
            RegistrationError.PROGRAM_NOT_FOUND -> R.string.registration_error_program_not_found
            RegistrationError.UNKNOWN_ERROR -> R.string.registration_error_unknown_error
        }
        is SurveillanceFormError -> when (this) {
            SurveillanceFormError.SITE_NOT_FOUND -> R.string.surveillance_form_error_site_not_found
            SurveillanceFormError.MISSING_PROGRAM_ID -> R.string.surveillance_form_error_missing_program_id
            SurveillanceFormError.MISSING_SESSION -> R.string.surveillance_form_error_missing_session
            SurveillanceFormError.LOCATION_PERMISSION_DENIED -> R.string.surveillance_form_error_location_permission_denied
            SurveillanceFormError.LOCATION_GPS_TIMEOUT -> R.string.surveillance_form_error_location_gps_timeout
            SurveillanceFormError.UNKNOWN_ERROR -> R.string.surveillance_form_error_unknown_error
        }
        is FormValidationError -> when (this) {
            FormValidationError.BLANK_COLLECTOR_TITLE -> R.string.form_validation_error_blank_collector_title
            FormValidationError.BLANK_COLLECTOR_NAME -> R.string.form_validation_error_blank_collector_name
            FormValidationError.BLANK_DISTRICT -> R.string.form_validation_error_blank_district
            FormValidationError.BLANK_SENTINEL_SITE -> R.string.form_validation_error_blank_sentinel_site
            FormValidationError.BLANK_HOUSE_NUMBER -> R.string.form_validation_error_blank_house_number
            FormValidationError.BLANK_LLIN_TYPE -> R.string.form_validation_error_blank_llin_type
            FormValidationError.BLANK_LLIN_BRAND -> R.string.form_validation_error_blank_llin_brand
            FormValidationError.FUTURE_COLLECTION_DATE -> R.string.form_validation_error_future_collection_date
            FormValidationError.BLANK_COLLECTION_METHOD -> R.string.form_validation_error_blank_collection_method
            FormValidationError.BLANK_SPECIMEN_CONDITION -> R.string.form_validation_error_blank_specimen_condition
        }
        else -> R.string.error_fallback
    }

    return context.getString(resId)
}