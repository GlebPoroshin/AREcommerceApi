package com.poroshin.rut.ar.api.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "baskets")
data class BasketDocument(
    @Id
    val id: String? = null,
    val userId: String,
    val items: MutableList<BasketItemDocument> = mutableListOf(),
)

data class BasketItemDocument(
    val sku: Long,
    var quantity: Int,
)
