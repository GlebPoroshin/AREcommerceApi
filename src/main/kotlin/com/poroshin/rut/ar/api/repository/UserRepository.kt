package com.poroshin.rut.ar.api.repository

import com.poroshin.rut.ar.api.entity.UserDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<UserDocument, String> {
    fun findByUserId(userId: String): UserDocument?
}
