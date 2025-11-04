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
    fun seedProducts(productRepository: ProductRepository): CommandLineRunner = CommandLineRunner {
        if (productRepository.count() > 0) {
            return@CommandLineRunner
        }

        val commonArMetadata = ArMetadata(
            version = 1002,
            arType = ArType.OBJECT,
            placement = ArPlacement.ANY_HORIZONTAL,
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
                imageUrl = "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/lmcode/kGthtXjO_EiIT47Y7XJboQ/92389573_01.jpg",
                oldPrice = "24 990",
                discount = 28,
                rate = 4.6,
                images = listOf(
                    "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/lmcode/kGthtXjO_EiIT47Y7XJboQ/92389573_01.jpg",
                    "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/lmcode/kGthtXjO_EiIT47Y7XJboQ/92389573_02.jpg",
                ),
                rating = 4.6,
                characteristics = mapOf(
                    "Материал" to "Велюр, дуб",
                    "Страна" to "Россия",
                    "Гарантия" to "24 мес.",
                ),
                stock = 12,
                deliveryInfo = "Доставим завтра",
                arMetadata = commonArMetadata,
            ),
            ProductDocument(
                sku = 1001L,
                name = "Кресло Loft",
                description = "Металл и кожа, минимализм",
                price = "12 400",
                imageUrl = "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/f_auto/q_auto/w_180/h_180/c_pad/b_white/d_photoiscoming.png/v1756302231/lmcode/ZNvykuHReEiS2JiysaTtfw/89428038.png",
                rate = 4.2,
                images = listOf(
                    "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/f_auto/q_auto/w_180/h_180/c_pad/b_white/d_photoiscoming.png/v1756302231/lmcode/ZNvykuHReEiS2JiysaTtfw/89428038.png",
                ),
                rating = 4.2,
                stock = 7,
                deliveryInfo = "Доставка в течение 3 дней",
                arMetadata = commonArMetadata,
            ),
            ProductDocument(
                sku = 1002L,
                name = "Стол Eames",
                description = "Стекло, бук, стиль mid-century",
                price = "22 990",
                imageUrl = "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/lmcode/aoz4PQriekCcblHRW7hgKg/92106858_01.jpg",
                oldPrice = "26 990",
                discount = 15,
                rate = 4.8,
                images = listOf(
                    "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/lmcode/aoz4PQriekCcblHRW7hgKg/92106858_01.jpg",
                ),
                rating = 4.8,
                characteristics = mapOf(
                    "Материал" to "Стекло, бук",
                    "Коллекция" to "Mid-century",
                ),
                stock = 4,
                deliveryInfo = "Самовывоз завтра",
                arMetadata = commonArMetadata,
            ),
            ProductDocument(
                sku = 1003L,
                name = "Тумба Nova",
                description = "Компактное хранение",
                price = "7 990",
                imageUrl = "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/lmcode/0xsaFgwZ0U6BXLKbAk16uA/90782652_01.jpg",
                rate = 4.0,
                images = listOf(
                    "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/lmcode/0xsaFgwZ0U6BXLKbAk16uA/90782652_01.jpg",
                ),
                rating = 4.0,
                stock = 15,
                deliveryInfo = "Доставим на этой неделе",
                arMetadata = commonArMetadata,
            ),
            ProductDocument(
                sku = 1004L,
                name = "Лампа Orbit",
                description = "Тёплый свет для уюта",
                price = "3 490",
                imageUrl = "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/f_auto/q_auto/w_180/h_180/c_pad/b_white/d_photoiscoming.png/v1756301982/lmcode/hHEINX8z0EuCLF3_MgoIPg/89428030.png",
                oldPrice = "4 290",
                discount = 19,
                rate = 3.9,
                images = listOf(
                    "https://cdn.lemanapro.ru/lmru/image/upload/dpr_2.0/f_auto/q_auto/w_180/h_180/c_pad/b_white/d_photoiscoming.png/v1756301982/lmcode/hHEINX8z0EuCLF3_MgoIPg/89428030.png",
                ),
                rating = 3.9,
                stock = 23,
                deliveryInfo = "Доступно в магазине",
                arMetadata = commonArMetadata,
            ),
        )

        productRepository.saveAll(products)
    }
}
