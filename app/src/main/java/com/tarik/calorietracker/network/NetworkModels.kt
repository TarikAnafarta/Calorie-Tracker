package com.tarik.calorietracker.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsResponse(
    val products: List<ProductItem> = emptyList()
)

@Serializable
data class ProductItem(
    @SerialName("product_name")
    val productName: String? = null,
    val nutriments: Nutriments? = null
)

@Serializable
data class Nutriments(
    @SerialName("energy-kcal_100g")
    val energyKcal100g: Float? = null
)

@Serializable
data class LocalFoodItem(
    val name: String,
    val aliases: List<String> = emptyList(),
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)