package com.poroshin.rut.ar.api.dto

import com.poroshin.rut.ar.api.model.ModelFormat

data class AdminUploadModelResponse(
    val sku: Long,
    val format: ModelFormat,
    val url: String,
)

data class AdminUploadImageResponse(
    val sku: Long,
    val url: String,
)

data class AdminErrorResponse(
    val error: String,
    val message: String,
)
