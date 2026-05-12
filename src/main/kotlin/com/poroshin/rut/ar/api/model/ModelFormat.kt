package com.poroshin.rut.ar.api.model

enum class ModelFormat(val extension: String, val contentType: String) {
    USDZ("usdz", "model/vnd.usdz+zip"),
    GLB("glb", "model/gltf-binary"),
}
