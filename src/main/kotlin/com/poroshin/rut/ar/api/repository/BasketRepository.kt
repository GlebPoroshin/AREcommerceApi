package com.poroshin.rut.ar.api.repository

import com.poroshin.rut.ar.api.entity.BasketDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BasketRepository : MongoRepository<BasketDocument, String> {
    fun findByUserId(userId: String): BasketDocument?
}
