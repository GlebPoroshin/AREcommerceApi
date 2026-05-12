package com.poroshin.rut.ar.api.controller

import com.ninjasquad.springmockk.MockkBean
import com.poroshin.rut.ar.api.config.AdminProperties
import com.poroshin.rut.ar.api.config.ApiKeyFilter
import com.poroshin.rut.ar.api.model.ModelFormat
import com.poroshin.rut.ar.api.service.AdminUploadService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [AdminController::class])
@Import(ApiKeyFilter::class)
@EnableConfigurationProperties(AdminProperties::class)
@TestPropertySource(properties = ["admin.api-key=test-secret-key"])
class AdminControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var adminUploadService: AdminUploadService

    @Test
    fun uploadModel_withoutApiKey_returnsUnauthorized() {
        val file = MockMultipartFile("file", "model.glb", "model/gltf-binary", ByteArray(10) { 1 })

        mockMvc.perform(
            multipart("/admin/upload/model")
                .file(file)
                .param("sku", "1000")
                .param("format", "GLB"),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun uploadModel_withWrongApiKey_returnsUnauthorized() {
        val file = MockMultipartFile("file", "model.glb", "model/gltf-binary", ByteArray(10) { 1 })

        mockMvc.perform(
            multipart("/admin/upload/model")
                .file(file)
                .param("sku", "1000")
                .param("format", "GLB")
                .header("X-Admin-Api-Key", "wrong-key"),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun uploadModel_withValidApiKey_returnsOkAndUrl() {
        val expectedUrl = "https://storage.example.com/ar-models/1000.glb"
        every {
            adminUploadService.uploadProductModel(1000L, ModelFormat.GLB, "model/gltf-binary", any())
        } returns expectedUrl
        val file = MockMultipartFile("file", "model.glb", "model/gltf-binary", ByteArray(10) { 1 })

        mockMvc.perform(
            multipart("/admin/upload/model")
                .file(file)
                .param("sku", "1000")
                .param("format", "GLB")
                .header("X-Admin-Api-Key", "test-secret-key"),
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.url").value(expectedUrl))
            .andExpect(jsonPath("$.sku").value(1000))
            .andExpect(jsonPath("$.format").value("GLB"))
    }

    @Test
    fun uploadImage_withValidApiKey_returnsOkAndUrl() {
        val expectedUrl = "https://storage.example.com/ar-images/2000.png"
        every {
            adminUploadService.uploadProductImage(2000L, "image/png", any())
        } returns expectedUrl
        val file = MockMultipartFile("file", "image.png", "image/png", ByteArray(10) { 1 })

        mockMvc.perform(
            multipart("/admin/upload/image")
                .file(file)
                .param("sku", "2000")
                .header("X-Admin-Api-Key", "test-secret-key"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.url").value(expectedUrl))
            .andExpect(jsonPath("$.sku").value(2000))
    }

    @Test
    fun uploadModel_serviceThrowsIllegalArgumentException_returnsBadRequest() {
        every {
            adminUploadService.uploadProductModel(any(), any(), any(), any())
        } throws IllegalArgumentException("Model file is empty")
        val file = MockMultipartFile("file", "model.glb", "model/gltf-binary", ByteArray(10) { 1 })

        mockMvc.perform(
            multipart("/admin/upload/model")
                .file(file)
                .param("sku", "1000")
                .param("format", "GLB")
                .header("X-Admin-Api-Key", "test-secret-key"),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("bad_request"))
            .andExpect(jsonPath("$.message").value("Model file is empty"))
    }

    @Test
    fun uploadModel_serviceThrowsIllegalStateException_returnsConflict() {
        every {
            adminUploadService.uploadProductModel(any(), any(), any(), any())
        } throws IllegalStateException("Product 1000 has no AR metadata; cannot attach model")
        val file = MockMultipartFile("file", "model.glb", "model/gltf-binary", ByteArray(10) { 1 })

        mockMvc.perform(
            multipart("/admin/upload/model")
                .file(file)
                .param("sku", "1000")
                .param("format", "GLB")
                .header("X-Admin-Api-Key", "test-secret-key"),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("conflict"))
    }
}
