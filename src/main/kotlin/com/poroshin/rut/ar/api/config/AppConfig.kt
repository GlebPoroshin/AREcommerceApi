package com.poroshin.rut.ar.api.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(YandexS3Properties::class, AdminProperties::class)
class AppConfig
