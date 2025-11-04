package com.poroshin.rut.ar.api.dto

data class BasketRequest(
    val userId: String,
    val sku: Long,
    val quantity: Int,
)

data class BasketResponse(
    val userId: String,
    val items: List<BasketItemDto>,
)

data class BasketItemDto(
    val sku: Long,
    val quantity: Int,
)
