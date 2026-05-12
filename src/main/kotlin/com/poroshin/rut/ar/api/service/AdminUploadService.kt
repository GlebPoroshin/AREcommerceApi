package com.poroshin.rut.ar.api.service

import com.poroshin.rut.ar.api.model.ModelFormat
import com.poroshin.rut.ar.api.repository.ObjectStorageRepository
import com.poroshin.rut.ar.api.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class AdminUploadService(
    private val objectStorageRepository: ObjectStorageRepository,
    private val productRepository: ProductRepository,
) {

    fun uploadProductModel(
        sku: Long,
        format: ModelFormat,
        contentType: String,
        bytes: ByteArray,
    ): String {
        require(bytes.isNotEmpty()) { "Model file is empty" }
        require(bytes.size <= MAX_MODEL_SIZE_BYTES) {
            "Model file is too large: ${bytes.size} bytes; max allowed is $MAX_MODEL_SIZE_BYTES bytes"
        }
        val normalizedContentType = contentType.lowercase()
        val expectedContentType = format.contentType
        require(normalizedContentType == expectedContentType) {
            "Content type '$contentType' does not match format $format; expected '$expectedContentType'"
        }

        val product = productRepository.findBySku(sku)
            ?: throw IllegalArgumentException("Product with sku $sku not found")

        val arMetadata = product.arMetadata
            ?: throw IllegalStateException("Product $sku has no AR metadata; cannot attach model")

        val url = objectStorageRepository.uploadModel(sku, format, bytes)

        val updatedArMetadata = when (format) {
            ModelFormat.GLB -> arMetadata.copy(arResourceUrlAndroid = url)
            ModelFormat.USDZ -> arMetadata.copy(arResourceUrlIos = url)
        }
        productRepository.save(product.copy(arMetadata = updatedArMetadata))

        return url
    }

    fun uploadProductImage(
        sku: Long,
        contentType: String,
        bytes: ByteArray,
    ): String {
        require(bytes.isNotEmpty()) { "Image file is empty" }
        require(bytes.size <= MAX_IMAGE_SIZE_BYTES) {
            "Image file is too large: ${bytes.size} bytes; max allowed is $MAX_IMAGE_SIZE_BYTES bytes"
        }
        val normalizedContentType = when (contentType.lowercase()) {
            "image/png" -> "image/png"
            "image/jpeg", "image/jpg" -> "image/jpeg"
            else -> throw IllegalArgumentException(
                "Unsupported image content type: $contentType; allowed: image/png, image/jpeg, image/jpg",
            )
        }

        val product = productRepository.findBySku(sku)
            ?: throw IllegalArgumentException("Product with sku $sku not found")

        val url = objectStorageRepository.uploadImage(sku, bytes, normalizedContentType)

        // TODO: support multiple images via dedicated endpoint; for now admin upload replaces the gallery.
        productRepository.save(
            product.copy(
                imageUrl = url,
                images = listOf(url),
            ),
        )

        return url
    }

    private companion object {
        const val MAX_MODEL_SIZE_BYTES = 50L * 1024 * 1024
        const val MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024
    }
}
