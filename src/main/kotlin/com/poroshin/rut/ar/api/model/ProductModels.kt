package com.poroshin.rut.ar.api.model

data class Product(
    val sku: Long,
    val name: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val oldPrice: String? = null,
    val discount: Int? = null,
    val rate: Double = 0.0,
)

data class ProductPageInfo(
    val sku: Long,
    val name: String,
    val description: String,
    val price: String,
    val images: List<String>,
    val oldPrice: String? = null,
    val discount: Int? = null,
    val rating: Double = 0.0,
    val characteristics: Map<String, String> = emptyMap(),
    val stock: Int? = null,
    val deliveryInfo: String? = null,
    val ar: ArInfo? = null,
)

data class ArInfo(
    val version: Int?,
    val arType: ArType,
    val placement: ArPlacement,
    val arRecourceUrl: String,
    val width: Float,
    val height: Float,
    val depth: Float? = null,
)

enum class ArType { OBJECT, FLOOR, WALL }

enum class ArPlacement {
    FLOOR,
    CEILING,
    ANY_HORIZONTAL,
    ANY_VERTICAL,
    ANY_SURFACE,
}

enum class OsType { ANDROID, IOS }
