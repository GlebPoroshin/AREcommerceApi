package com.poroshin.rut.ar.api.repository

import com.poroshin.rut.ar.api.config.YandexS3Properties
import com.poroshin.rut.ar.api.model.ModelFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Repository
class ObjectStorageRepository(
    private val s3Client: S3Client,
    private val props: YandexS3Properties,
) {

    private val log = LoggerFactory.getLogger(ObjectStorageRepository::class.java)

    fun uploadModel(sku: Long, format: ModelFormat, bytes: ByteArray): String {
        val bucket = props.buckets.models
        val key = "$sku.${format.extension}"
        putObject(bucket, key, format.contentType, bytes)
        return buildUrl(bucket, key)
    }

    fun uploadImage(sku: Long, bytes: ByteArray, contentType: String): String {
        val bucket = props.buckets.images
        val extension = extensionFromContentType(contentType)
        val key = "$sku.$extension"
        putObject(bucket, key, contentType, bytes)
        return buildUrl(bucket, key)
    }

    private fun putObject(bucket: String, key: String, contentType: String, bytes: ByteArray) {
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(bytes.size.toLong())
            .build()
        s3Client.putObject(request, RequestBody.fromBytes(bytes))
        log.info("Uploaded $bucket/$key (${bytes.size} bytes)")
    }

    private fun buildUrl(bucket: String, key: String): String =
        "${props.endpoint.trimEnd('/')}/$bucket/$key"

    private fun extensionFromContentType(contentType: String): String =
        when (contentType.lowercase()) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            else -> throw IllegalArgumentException("Unsupported image content type: $contentType")
        }
}
