package com.poroshin.rut.ar.api.service

import com.poroshin.rut.ar.api.entity.ArMetadata
import com.poroshin.rut.ar.api.entity.ProductDocument
import com.poroshin.rut.ar.api.model.ArPlacement
import com.poroshin.rut.ar.api.model.ArType
import com.poroshin.rut.ar.api.model.ModelFormat
import com.poroshin.rut.ar.api.repository.ObjectStorageRepository
import com.poroshin.rut.ar.api.repository.ProductRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AdminUploadServiceTest {

    private val objectStorageRepository: ObjectStorageRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private val service = AdminUploadService(objectStorageRepository, productRepository)

    private val baseArMetadata = ArMetadata(
        version = 1,
        arType = ArType.OBJECT,
        placement = ArPlacement.FLOOR,
        arResourceUrlAndroid = null,
        arResourceUrlIos = null,
        width = 1.0f,
        height = 2.0f,
        depth = 0.5f,
    )

    private fun productWithAr(sku: Long = 1000L, ar: ArMetadata? = baseArMetadata) = ProductDocument(
        id = "id-$sku",
        sku = sku,
        name = "Test product",
        description = "desc",
        price = "100",
        imageUrl = "https://example.com/old.png",
        arMetadata = ar,
    )

    // -------- Test byte arrays with valid magic signatures --------

    private val glbBytes: ByteArray = byteArrayOf(0x67, 0x6C, 0x54, 0x46) + ByteArray(100)
    private val pngBytes: ByteArray = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
    ) + ByteArray(100)
    private val jpegBytes: ByteArray = byteArrayOf(
        0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(),
    ) + ByteArray(100)
    private val webpBytes: ByteArray = byteArrayOf(
        0x52, 0x49, 0x46, 0x46,
        0x00, 0x00, 0x00, 0x00,
        0x57, 0x45, 0x42, 0x50,
    ) + ByteArray(100)
    private val usdzBytes: ByteArray = ByteArray(10) { 2 }

    // -------- uploadProductModel --------

    @Test
    fun uploadProductModel_glbHappyPath_savesAndroidUrl() {
        val sku = 1000L
        val expectedUrl = "https://storage.example/models/$sku.glb"
        val product = productWithAr(sku)
        every { productRepository.findBySku(sku) } returns product
        every { objectStorageRepository.uploadModel(sku, ModelFormat.GLB, glbBytes) } returns expectedUrl
        val saved = slot<ProductDocument>()
        every { productRepository.save(capture(saved)) } answers { saved.captured }

        val result = service.uploadProductModel(sku, ModelFormat.GLB, "model/gltf-binary", glbBytes)

        assertEquals(expectedUrl, result)
        assertEquals(expectedUrl, saved.captured.arMetadata?.arResourceUrlAndroid)
        assertEquals(null, saved.captured.arMetadata?.arResourceUrlIos)
        verify(exactly = 1) { objectStorageRepository.uploadModel(sku, ModelFormat.GLB, glbBytes) }
        verify(exactly = 1) { productRepository.save(any()) }
    }

    @Test
    fun uploadProductModel_usdzHappyPath_savesIosUrl() {
        val sku = 1001L
        val expectedUrl = "https://storage.example/models/$sku.usdz"
        val product = productWithAr(sku)
        every { productRepository.findBySku(sku) } returns product
        every { objectStorageRepository.uploadModel(sku, ModelFormat.USDZ, usdzBytes) } returns expectedUrl
        val saved = slot<ProductDocument>()
        every { productRepository.save(capture(saved)) } answers { saved.captured }

        val result = service.uploadProductModel(sku, ModelFormat.USDZ, "model/vnd.usdz+zip", usdzBytes)

        assertEquals(expectedUrl, result)
        assertEquals(expectedUrl, saved.captured.arMetadata?.arResourceUrlIos)
        assertEquals(null, saved.captured.arMetadata?.arResourceUrlAndroid)
    }

    @Test
    fun uploadProductModel_emptyBytes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductModel(1000L, ModelFormat.GLB, "model/gltf-binary", ByteArray(0))
        }
    }

    @Test
    fun uploadProductModel_bytesAboveMaxSize_throwsIllegalArgumentException() {
        val tooLarge = ByteArray(50 * 1024 * 1024 + 1)
        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductModel(1000L, ModelFormat.GLB, "model/gltf-binary", tooLarge)
        }
    }

    @Test
    fun uploadProductModel_contentTypeMismatch_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductModel(1000L, ModelFormat.GLB, "image/png", ByteArray(10))
        }
    }

    @Test
    fun uploadProductModel_invalidGlbSignature_throwsIllegalArgumentException() {
        val sku = 1000L
        val invalidBytes = ByteArray(10) { 0xFF.toByte() }
        every { productRepository.findBySku(sku) } returns productWithAr(sku)

        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductModel(sku, ModelFormat.GLB, "model/gltf-binary", invalidBytes)
        }
    }

    @Test
    fun uploadProductModel_productNotFound_throwsIllegalArgumentException() {
        val sku = 9999L
        every { productRepository.findBySku(sku) } returns null

        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductModel(sku, ModelFormat.GLB, "model/gltf-binary", glbBytes)
        }
    }

    @Test
    fun uploadProductModel_productWithoutArMetadata_throwsIllegalStateException() {
        val sku = 1000L
        every { productRepository.findBySku(sku) } returns productWithAr(sku, ar = null)

        assertThrows(IllegalStateException::class.java) {
            service.uploadProductModel(sku, ModelFormat.GLB, "model/gltf-binary", glbBytes)
        }
    }

    // -------- uploadProductImage --------

    @Test
    fun uploadProductImage_pngHappyPath_savesImageAndImagesList() {
        val sku = 2000L
        val expectedUrl = "https://storage.example/images/$sku.png"
        val product = productWithAr(sku)
        every { productRepository.findBySku(sku) } returns product
        every { objectStorageRepository.uploadImage(sku, pngBytes, "image/png") } returns expectedUrl
        val saved = slot<ProductDocument>()
        every { productRepository.save(capture(saved)) } answers { saved.captured }

        val result = service.uploadProductImage(sku, "image/png", pngBytes)

        assertEquals(expectedUrl, result)
        assertEquals(expectedUrl, saved.captured.imageUrl)
        assertEquals(listOf(expectedUrl), saved.captured.images)
        verify(exactly = 1) { objectStorageRepository.uploadImage(sku, pngBytes, "image/png") }
    }

    @Test
    fun uploadProductImage_jpegContentType_passesNormalizedContentTypeToRepository() {
        val sku = 2001L
        val expectedUrl = "https://storage.example/images/$sku.jpg"
        val product = productWithAr(sku)
        every { productRepository.findBySku(sku) } returns product
        every { objectStorageRepository.uploadImage(sku, jpegBytes, "image/jpeg") } returns expectedUrl
        every { productRepository.save(any()) } answers { firstArg() }

        val result = service.uploadProductImage(sku, "image/jpeg", jpegBytes)

        assertEquals(expectedUrl, result)
        verify(exactly = 1) { objectStorageRepository.uploadImage(sku, jpegBytes, "image/jpeg") }
    }

    @Test
    fun uploadProductImage_jpgContentType_normalizedToJpeg() {
        val sku = 2002L
        val expectedUrl = "https://storage.example/images/$sku.jpg"
        val product = productWithAr(sku)
        every { productRepository.findBySku(sku) } returns product
        every { objectStorageRepository.uploadImage(sku, jpegBytes, "image/jpeg") } returns expectedUrl
        every { productRepository.save(any()) } answers { firstArg() }

        val result = service.uploadProductImage(sku, "image/jpg", jpegBytes)

        assertEquals(expectedUrl, result)
        verify(exactly = 1) { objectStorageRepository.uploadImage(sku, jpegBytes, "image/jpeg") }
    }

    @Test
    fun uploadProductImage_invalidPngSignature_throwsIllegalArgumentException() {
        val sku = 2000L
        val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFF.toByte()) + ByteArray(100)
        every { productRepository.findBySku(sku) } returns productWithAr(sku)

        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductImage(sku, "image/png", invalidBytes)
        }
    }

    @Test
    fun uploadProductImage_invalidJpegSignature_throwsIllegalArgumentException() {
        val sku = 2001L
        val invalidBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E) + ByteArray(100)
        every { productRepository.findBySku(sku) } returns productWithAr(sku)

        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductImage(sku, "image/jpeg", invalidBytes)
        }
    }

    @Test
    fun uploadProductImage_webpHappyPath_savesImageUrl() {
        val sku = 2003L
        val expectedUrl = "https://storage.example/images/$sku.webp"
        val product = productWithAr(sku)
        every { productRepository.findBySku(sku) } returns product
        every { objectStorageRepository.uploadImage(sku, webpBytes, "image/webp") } returns expectedUrl
        every { productRepository.save(any()) } answers { firstArg() }

        val result = service.uploadProductImage(sku, "image/webp", webpBytes)

        assertEquals(expectedUrl, result)
        verify(exactly = 1) { objectStorageRepository.uploadImage(sku, webpBytes, "image/webp") }
    }

    @Test
    fun uploadProductImage_emptyBytes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductImage(2000L, "image/png", ByteArray(0))
        }
    }

    @Test
    fun uploadProductImage_bytesAboveMaxSize_throwsIllegalArgumentException() {
        val tooLarge = ByteArray(5 * 1024 * 1024 + 1)
        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductImage(2000L, "image/png", tooLarge)
        }
    }

    @Test
    fun uploadProductImage_unsupportedContentType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductImage(2000L, "application/octet-stream", ByteArray(10))
        }
    }

    @Test
    fun uploadProductImage_productNotFound_throwsIllegalArgumentException() {
        val sku = 9999L
        every { productRepository.findBySku(sku) } returns null

        assertThrows(IllegalArgumentException::class.java) {
            service.uploadProductImage(sku, "image/png", pngBytes)
        }
    }
}
