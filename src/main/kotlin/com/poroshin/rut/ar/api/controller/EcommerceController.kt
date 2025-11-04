package com.poroshin.rut.ar.api.controller

import com.poroshin.rut.ar.api.dto.BasketRequest
import com.poroshin.rut.ar.api.dto.BasketResponse
import com.poroshin.rut.ar.api.model.OsType
import com.poroshin.rut.ar.api.model.Product
import com.poroshin.rut.ar.api.model.ProductPageInfo
import com.poroshin.rut.ar.api.service.EcommerceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class EcommerceController(
    private val ecommerceService: EcommerceService,
) {

    @GetMapping("/createUserId")
    fun createUserId(): ResponseEntity<String> =
        ResponseEntity.ok(ecommerceService.createUserId())

    @GetMapping("/plp")
    fun getPlp(): List<Product> = ecommerceService.getPlp()

    @GetMapping("/pdp/{sku}")
    fun getPdp(
        @PathVariable sku: Long,
        @RequestParam(required = false) osType: OsType?,
    ): ProductPageInfo = ecommerceService.getPdp(sku, osType)

    @PostMapping("/basket")
    fun addToBasket(@RequestBody request: BasketRequest): ResponseEntity<BasketResponse> =
        ResponseEntity.ok(ecommerceService.addToBasket(request))

    @GetMapping("/basket")
    fun getBasket(@RequestParam userId: String): BasketResponse =
        ecommerceService.getBasket(userId)
}
