package com.poroshin.rut.ar.api.service

object FileSignatureValidator {

    private val GLB_MAGIC = byteArrayOf(0x67, 0x6C, 0x54, 0x46)       // "glTF"
    private val PNG_MAGIC = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    private val JPEG_MAGIC = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val RIFF_MAGIC = byteArrayOf(0x52, 0x49, 0x46, 0x46)       // "RIFF"
    private val WEBP_MARKER = byteArrayOf(0x57, 0x45, 0x42, 0x50)      // "WEBP" at offset 8

    fun validateGlb(bytes: ByteArray) {
        require(bytes.size >= 4 && bytes.startsWith(GLB_MAGIC)) {
            "File content does not match GLB format: expected glTF magic bytes"
        }
    }

    fun validateGltf(bytes: ByteArray) {
        val start = bytes.take(16).toByteArray().toString(Charsets.UTF_8).trimStart()
        require(start.startsWith("{")) {
            "File content does not match GLTF format: expected JSON object"
        }
    }

    fun validatePng(bytes: ByteArray) {
        require(bytes.size >= 8 && bytes.startsWith(PNG_MAGIC)) {
            "File content does not match PNG format: invalid magic bytes"
        }
    }

    fun validateJpeg(bytes: ByteArray) {
        require(bytes.size >= 3 && bytes.startsWith(JPEG_MAGIC)) {
            "File content does not match JPEG format: invalid magic bytes"
        }
    }

    fun validateWebp(bytes: ByteArray) {
        require(
            bytes.size >= 12 &&
                bytes.startsWith(RIFF_MAGIC) &&
                bytes.sliceArray(8..11).contentEquals(WEBP_MARKER),
        ) {
            "File content does not match WebP format: invalid RIFF/WEBP signature"
        }
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (size < prefix.size) return false
        return prefix.indices.all { this[it] == prefix[it] }
    }
}
