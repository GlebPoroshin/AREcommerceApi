package com.poroshin.rut.ar.api.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class S3Config {

    private val log = LoggerFactory.getLogger(S3Config::class.java)

    @Bean(destroyMethod = "close")
    fun s3Client(props: YandexS3Properties): S3Client {
        val credentialsProvider: AwsCredentialsProvider =
            if (props.accessKeyId.isBlank() || props.secretAccessKey.isBlank()) {
                log.warn("Yandex S3 credentials are blank — uploads will fail at runtime")
                AnonymousCredentialsProvider.create()
            } else {
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.accessKeyId, props.secretAccessKey),
                )
            }

        return S3Client.builder()
            .endpointOverride(URI.create(props.endpoint))
            .region(Region.of(props.region))
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build(),
            )
            .build()
    }
}
