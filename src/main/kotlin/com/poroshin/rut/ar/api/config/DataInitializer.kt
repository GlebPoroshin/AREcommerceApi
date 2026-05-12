package com.poroshin.rut.ar.api.config

import com.poroshin.rut.ar.api.entity.ArMetadata
import com.poroshin.rut.ar.api.entity.ProductDocument
import com.poroshin.rut.ar.api.model.ArPlacement
import com.poroshin.rut.ar.api.model.ArType
import com.poroshin.rut.ar.api.repository.ProductRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {

    @Bean
    fun seedProducts(
        productRepository: ProductRepository,
        yandexS3Properties: YandexS3Properties,
    ): CommandLineRunner = CommandLineRunner {
        fun imageUrlFor(sku: Long): String =
            "${yandexS3Properties.endpoint}/${yandexS3Properties.buckets.images}/$sku.png"

        fun arMetadataFor(sku: Long): ArMetadata = ArMetadata(
            version = 1002,
            arType = ArType.OBJECT,
            placement = ArPlacement.ANY_HORIZONTAL,
            arResourceUrlAndroid = "${yandexS3Properties.endpoint}/${yandexS3Properties.buckets.models}/$sku.glb",
            arResourceUrlIos = "${yandexS3Properties.endpoint}/${yandexS3Properties.buckets.models}/$sku.usdz",
            width = 950f,
            height = 1000f,
            depth = 950f,
        )

        val products = listOf(
            ProductDocument(
                sku = 1000L,
                name = "Диван Skandi",
                description = "Мягкий велюр, дубовые ножки",
                price = "18 000",
                imageUrl = imageUrlFor(1000L),
                oldPrice = "24 990",
                discount = 28,
                rate = 4.6,
                images = listOf(imageUrlFor(1000L)),
                rating = 4.6,
                characteristics = mapOf(
                    "Материал" to "Велюр, дуб",
                    "Страна" to "Россия",
                    "Гарантия" to "24 мес.",
                ),
                stock = 12,
                deliveryInfo = "Доставим завтра",
                arMetadata = arMetadataFor(1000L),
            ),
            ProductDocument(
                sku = 1001L,
                name = "Кресло Loft",
                description = "Металл и кожа, минимализм",
                price = "12 400",
                imageUrl = imageUrlFor(1001L),
                rate = 4.2,
                images = listOf(imageUrlFor(1001L)),
                rating = 4.2,
                stock = 7,
                deliveryInfo = "Доставка в течение 3 дней",
                arMetadata = arMetadataFor(1001L),
            ),
            ProductDocument(
                sku = 1002L,
                name = "Стол Eames",
                description = "Стекло, бук, стиль mid-century",
                price = "22 990",
                imageUrl = imageUrlFor(1002L),
                oldPrice = "26 990",
                discount = 15,
                rate = 4.8,
                images = listOf(imageUrlFor(1002L)),
                rating = 4.8,
                characteristics = mapOf(
                    "Материал" to "Стекло, бук",
                    "Коллекция" to "Mid-century",
                ),
                stock = 4,
                deliveryInfo = "Самовывоз завтра",
                arMetadata = arMetadataFor(1002L),
            ),
            ProductDocument(
                sku = 1003L,
                name = "Тумба Nova",
                description = "Компактное хранение",
                price = "7 990",
                imageUrl = imageUrlFor(1003L),
                rate = 4.0,
                images = listOf(imageUrlFor(1003L)),
                rating = 4.0,
                stock = 15,
                deliveryInfo = "Доставим на этой неделе",
                arMetadata = arMetadataFor(1003L),
            ),
            ProductDocument(
                sku = 1004L,
                name = "Лампа Orbit",
                description = "Тёплый свет для уюта",
                price = "3 490",
                imageUrl = imageUrlFor(1004L),
                oldPrice = "4 290",
                discount = 19,
                rate = 3.9,
                images = listOf(imageUrlFor(1004L)),
                rating = 3.9,
                stock = 23,
                deliveryInfo = "Доступно в магазине",
                arMetadata = arMetadataFor(1004L),
            ),
        )

        val existingProducts = productRepository.findAll()
        val existingBySku = existingProducts.associateBy { it.sku }
        val targetSkus = products.map { it.sku }.toSet()

        val synchronizedProducts = products.map { product ->
            val existingId = existingBySku[product.sku]?.id
            if (existingId != null) product.copy(id = existingId) else product
        }

        productRepository.saveAll(synchronizedProducts)

        val staleIds = existingProducts
            .filter { it.sku !in targetSkus }
            .mapNotNull { it.id }
        if (staleIds.isNotEmpty()) {
            productRepository.deleteAllById(staleIds)
        }
    }
}
