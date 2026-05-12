package com.poroshin.rut.ar.api.repository

import com.poroshin.rut.ar.api.config.YandexS3Properties
import com.poroshin.rut.ar.api.model.ModelFormat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse

class ObjectStorageRepositoryTest {

    private val s3Client: S3Client = mockk()
    private val props = YandexS3Properties(
        endpoint = "https://storage.example.com",
        region = "ru-central1",
        accessKeyId = "ak",
        secretAccessKey = "sk",
        buckets = YandexS3Properties.Buckets(
            models = "ar-models",
            images = "ar-images",
        ),
    )
    private val repository = ObjectStorageRepository(s3Client, props)

    @Test
    fun uploadModel_glbFormat_buildsCorrectRequestAndReturnsUrl() {
        val sku = 1234L
        val bytes = ByteArray(8) { 7 }
        val requestSlot = slot<PutObjectRequest>()
        every {
            s3Client.putObject(capture(requestSlot), any<RequestBody>())
        } returns PutObjectResponse.builder().build()

        val url = repository.uploadModel(sku, ModelFormat.GLB, bytes)

        assertEquals("https://storage.example.com/ar-models/1234.glb", url)
        val captured = requestSlot.captured
        assertEquals("ar-models", captured.bucket())
        assertEquals("1234.glb", captured.key())
        assertEquals("model/gltf-binary", captured.contentType())
        assertEquals(bytes.size.toLong(), captured.contentLength())
        verify(exactly = 1) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
    }

    @Test
    fun uploadModel_usdzFormat_buildsCorrectRequestAndReturnsUrl() {
        val sku = 5555L
        val bytes = ByteArray(4) { 9 }
        val requestSlot = slot<PutObjectRequest>()
        every {
            s3Client.putObject(capture(requestSlot), any<RequestBody>())
        } returns PutObjectResponse.builder().build()

        val url = repository.uploadModel(sku, ModelFormat.USDZ, bytes)

        assertEquals("https://storage.example.com/ar-models/5555.usdz", url)
        val captured = requestSlot.captured
        assertEquals("ar-models", captured.bucket())
        assertEquals("5555.usdz", captured.key())
        assertEquals("model/vnd.usdz+zip", captured.contentType())
    }

    @Test
    fun uploadModel_endpointWithTrailingSlash_isTrimmedInUrl() {
        val propsWithSlash = props.copy(endpoint = "https://storage.example.com/")
        val repo = ObjectStorageRepository(s3Client, propsWithSlash)
        every { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns
            PutObjectResponse.builder().build()

        val url = repo.uploadModel(1L, ModelFormat.GLB, ByteArray(1))

        assertEquals("https://storage.example.com/ar-models/1.glb", url)
    }

    @Test
    fun uploadImage_pngContentType_buildsCorrectRequestAndReturnsUrl() {
        val sku = 7777L
        val bytes = ByteArray(3) { 1 }
        val requestSlot = slot<PutObjectRequest>()
        every {
            s3Client.putObject(capture(requestSlot), any<RequestBody>())
        } returns PutObjectResponse.builder().build()

        val url = repository.uploadImage(sku, bytes, "image/png")

        assertEquals("https://storage.example.com/ar-images/7777.png", url)
        val captured = requestSlot.captured
        assertEquals("ar-images", captured.bucket())
        assertEquals("7777.png", captured.key())
        assertEquals("image/png", captured.contentType())
    }

    @Test
    fun uploadImage_jpegContentType_buildsCorrectRequestAndReturnsUrl() {
        val sku = 8888L
        val bytes = ByteArray(3) { 2 }
        val requestSlot = slot<PutObjectRequest>()
        every {
            s3Client.putObject(capture(requestSlot), any<RequestBody>())
        } returns PutObjectResponse.builder().build()

        val url = repository.uploadImage(sku, bytes, "image/jpeg")

        assertEquals("https://storage.example.com/ar-images/8888.jpg", url)
        val captured = requestSlot.captured
        assertEquals("ar-images", captured.bucket())
        assertEquals("8888.jpg", captured.key())
        assertEquals("image/jpeg", captured.contentType())
    }

    @Test
    fun uploadImage_unsupportedContentType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            repository.uploadImage(1L, ByteArray(1), "application/octet-stream")
        }
    }
}
