package com.poroshin.rut.ar.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "yandex-s3")
data class YandexS3Properties(
    val endpoint: String,
    val region: String,
    val accessKeyId: String,
    val secretAccessKey: String,
    val buckets: Buckets,
) {
    data class Buckets(
        val models: String,
        val images: String,
    )
}
