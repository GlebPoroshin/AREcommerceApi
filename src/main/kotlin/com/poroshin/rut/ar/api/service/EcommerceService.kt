package com.poroshin.rut.ar.api.service

import com.poroshin.rut.ar.api.dto.BasketItemDto
import com.poroshin.rut.ar.api.dto.BasketRequest
import com.poroshin.rut.ar.api.dto.BasketResponse
import com.poroshin.rut.ar.api.entity.BasketDocument
import com.poroshin.rut.ar.api.entity.BasketItemDocument
import com.poroshin.rut.ar.api.entity.ProductDocument
import com.poroshin.rut.ar.api.entity.UserDocument
import com.poroshin.rut.ar.api.model.ArInfo
import com.poroshin.rut.ar.api.model.ArPlacement
import com.poroshin.rut.ar.api.model.ArType
import com.poroshin.rut.ar.api.model.OsType
import com.poroshin.rut.ar.api.model.Product
import com.poroshin.rut.ar.api.model.ProductPageInfo
import com.poroshin.rut.ar.api.repository.BasketRepository
import com.poroshin.rut.ar.api.repository.ProductRepository
import com.poroshin.rut.ar.api.repository.UserRepository
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class EcommerceService(
    private val productRepository: ProductRepository,
    private val basketRepository: BasketRepository,
    private val userRepository: UserRepository,
) {

    fun createUserId(): String {
        var userId: String
        do {
            userId = UUID.randomUUID().toString()
        } while (userRepository.findByUserId(userId) != null)

        userRepository.save(UserDocument(userId = userId))
        return userId
    }

    fun getPlp(): List<Product> {
        return productRepository
            .findAll()
            .sortedBy { it.sku }
            .map { it.toProduct() }
    }

    fun getPdp(sku: Long, osType: OsType?): ProductPageInfo {
        val targetOs = osType ?: OsType.ANDROID
        val document = productRepository.findBySku(sku)
        return document?.toProductPageInfo(targetOs) ?: buildFallbackProductPageInfo(sku, targetOs)
    }

    fun addToBasket(request: BasketRequest): BasketResponse {
        require(request.quantity > 0) { "Quantity must be positive" }

        ensureUserExists(request.userId)

        val basket = basketRepository.findByUserId(request.userId)
            ?: BasketDocument(userId = request.userId, items = mutableListOf())

        val existingItem = basket.items.firstOrNull { it.sku == request.sku }
        if (existingItem != null) {
            existingItem.quantity = request.quantity
        } else {
            basket.items.add(BasketItemDocument(request.sku, request.quantity))
        }

        val saved = basketRepository.save(basket)
        return saved.toResponse()
    }

    fun getBasket(userId: String): BasketResponse {
        ensureUserExists(userId)
        val basket = basketRepository.findByUserId(userId)
            ?: BasketDocument(userId = userId, items = mutableListOf())
        return basket.toResponse()
    }

    private fun ProductDocument.toProduct(): Product {
        return Product(
            sku = sku,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl,
            oldPrice = oldPrice,
            discount = discount,
            rate = rate,
        )
    }

    private fun ProductDocument.toProductPageInfo(osType: OsType): ProductPageInfo {
        val metadata = arMetadata
        val arInfo = metadata?.let {
            ArInfo(
                version = it.version,
                arType = it.arType,
                placement = it.placement,
                arRecourceUrl = resolveArResourceUrl(osType),
                width = it.width,
                height = it.height,
                depth = it.depth,
            )
        }

        return ProductPageInfo(
            sku = sku,
            name = name,
            description = description,
            price = price,
            images = images.takeIf { it.isNotEmpty() } ?: listOf(imageUrl),
            oldPrice = oldPrice,
            discount = discount,
            rating = if (rating != 0.0) rating else rate,
            characteristics = characteristics,
            stock = stock,
            deliveryInfo = deliveryInfo,
            ar = arInfo,
        )
    }

    private fun BasketDocument.toResponse(): BasketResponse {
        val itemsDto = items.map { BasketItemDto(sku = it.sku, quantity = it.quantity) }
        return BasketResponse(
            userId = userId,
            items = itemsDto,
        )
    }

    private fun buildFallbackProductPageInfo(sku: Long, osType: OsType): ProductPageInfo {
        val imageUrl = "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/lmcode/kGthtXjO_EiIT47Y7XJboQ/92389573_01.jpg"
        val arUrl = resolveArResourceUrl(osType)

        return ProductPageInfo(
            sku = sku,
            name = "Диван Skandi",
            description = "Мягкий велюр, дубовые ножки",
            price = "18 000",
            images = listOf(imageUrl),
            oldPrice = "24 990",
            discount = 28,
            rating = 4.6,
            characteristics = mapOf(
                "Материал" to "Велюр, дуб",
                "Страна" to "Россия",
                "Гарантия" to "24 мес.",
            ),
            stock = 12,
            deliveryInfo = "Доставим завтра",
            ar = ArInfo(
                arType = ArType.OBJECT,
                placement = ArPlacement.ANY_HORIZONTAL,
                arRecourceUrl = arUrl,
                version = 1002,
                width = 950f,
                height = 1000f,
                depth = 950f,
            ),
        )
    }

    private fun resolveArResourceUrl(osType: OsType): String {
        return when (osType) {
            OsType.ANDROID -> "https://storage.yandexcloud.net/ar-app/models/AR-Code-1683007596576.glb"
            OsType.IOS -> "https://storage.yandexcloud.net/ar-app/models/AR-Code-1683007596576.usdz"
        }
    }

    private fun ensureUserExists(userId: String) {
        if (userRepository.findByUserId(userId) == null) {
            userRepository.save(UserDocument(userId = userId))
        }
    }
}
