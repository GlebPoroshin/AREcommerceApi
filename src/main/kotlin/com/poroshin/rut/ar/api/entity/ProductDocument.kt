package com.poroshin.rut.ar.api.entity

import com.poroshin.rut.ar.api.model.ArPlacement
import com.poroshin.rut.ar.api.model.ArType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "products")
data class ProductDocument(
    @Id
    val id: String? = null,
    val sku: Long,
    val name: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val oldPrice: String? = null,
    val discount: Int? = null,
    val rate: Double = 0.0,
    val images: List<String> = emptyList(),
    val rating: Double = 0.0,
    val characteristics: Map<String, String> = emptyMap(),
    val stock: Int? = null,
    val deliveryInfo: String? = null,
    val arMetadata: ArMetadata? = null,
)

data class ArMetadata(
    val version: Int?,
    val arType: ArType,
    val placement: ArPlacement,
    val arResourceUrlAndroid: String? = null,
    val arResourceUrlIos: String? = null,
    val width: Float,
    val height: Float,
    val depth: Float? = null,
)
