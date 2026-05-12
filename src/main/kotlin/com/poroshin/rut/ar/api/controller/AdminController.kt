package com.poroshin.rut.ar.api.controller

import com.poroshin.rut.ar.api.dto.AdminErrorResponse
import com.poroshin.rut.ar.api.dto.AdminUploadImageResponse
import com.poroshin.rut.ar.api.dto.AdminUploadModelResponse
import com.poroshin.rut.ar.api.model.ModelFormat
import com.poroshin.rut.ar.api.service.AdminUploadService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminUploadService: AdminUploadService,
) {

    @PostMapping("/upload/model", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadModel(
        @RequestParam sku: Long,
        @RequestParam format: ModelFormat,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<AdminUploadModelResponse> {
        val contentType = file.contentType
            ?: throw IllegalArgumentException("Missing Content-Type for file part")
        val url = adminUploadService.uploadProductModel(sku, format, contentType, file.bytes)
        return ResponseEntity.ok(AdminUploadModelResponse(sku, format, url))
    }

    @PostMapping("/upload/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestParam sku: Long,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<AdminUploadImageResponse> {
        val contentType = file.contentType
            ?: throw IllegalArgumentException("Missing Content-Type for file part")
        val url = adminUploadService.uploadProductImage(sku, contentType, file.bytes)
        return ResponseEntity.ok(AdminUploadImageResponse(sku, url))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<AdminErrorResponse> =
        ResponseEntity.badRequest().body(
            AdminErrorResponse(
                error = "bad_request",
                message = ex.message ?: "Invalid request",
            ),
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<AdminErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            AdminErrorResponse(
                error = "conflict",
                message = ex.message ?: "Conflict",
            ),
        )
}
