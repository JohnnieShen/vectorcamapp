package com.vci.vectorcamapp.surveillance_form.presentation.util

import android.content.Context
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.surveillance_form.domain.util.FormValidationError

fun FormValidationError.toString(context: Context): String {
    val resId = when(this) {
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
    return context.getString(resId)
}
