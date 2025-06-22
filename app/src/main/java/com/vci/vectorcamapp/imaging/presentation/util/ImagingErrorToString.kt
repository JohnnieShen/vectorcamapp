package com.vci.vectorcamapp.imaging.presentation.util

import android.content.Context
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.imaging.domain.util.ImagingError

fun ImagingError.toString(context: Context): String {
    val resId = when(this) {
        ImagingError.CAPTURE_ERROR -> R.string.imaging_error_capture_error
        ImagingError.SAVE_ERROR -> R.string.imaging_error_save_error
        ImagingError.NO_SPECIMEN_FOUND -> R.string.imaging_error_no_specimen_found
        ImagingError.MULTIPLE_SPECIMENS_FOUND -> R.string.imaging_error_multiple_specimens_found
        ImagingError.NO_ACTIVE_SESSION -> R.string.imaging_error_no_active_session
        ImagingError.MODEL_INITIALIZATION_FAILED -> R.string.imaging_error_model_initialization_failed
        ImagingError.GPU_DELEGATE_INITIALIZATION_FAILED -> R.string.imaging_error_gpu_delegate_initialization_failed
        ImagingError.UNKNOWN_INITIALIZATION_ERROR -> R.string.imaging_error_unknown_initialization_error
        ImagingError.INVALID_INPUT_SHAPE -> R.string.imaging_error_invalid_input_shape
        ImagingError.SPECIMEN_ID_RECOGNITION_FAILED -> R.string.imaging_error_specimen_id_recognition_failed
        ImagingError.SPECIMEN_DETECTION_FAILED -> R.string.imaging_error_specimen_detection_failed
        ImagingError.SPECIMEN_CLASSIFICATION_FAILED -> R.string.imaging_error_specimen_classification_failed
        ImagingError.UNKNOWN_INFERENCE_ERROR -> R.string.imaging_error_unknown_inference_error
        ImagingError.UNKNOWN -> R.string.imaging_error_unknown
    }
    return context.getString(resId)
}
