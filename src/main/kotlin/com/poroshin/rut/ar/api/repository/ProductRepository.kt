package com.poroshin.rut.ar.api.repository

import com.poroshin.rut.ar.api.entity.ProductDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : MongoRepository<ProductDocument, String> {
    fun findBySku(sku: Long): ProductDocument?
}
