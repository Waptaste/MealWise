package com.example.mealwise.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface HdxApiService {
    @GET("food-security-nutrition-poverty/food-prices")
    suspend fun getFoodPrices(
        @Query("location_code") locationCode: String = "ZMB",
        @Query("output_format") outputFormat: String = "json"
    ): HdxResponse
}

data class HdxResponse(
    @SerializedName("data") val data: List<HdxPriceData>
)

data class HdxPriceData(
    @SerializedName("commodity_name") val commodityName: String,
    @SerializedName("price") val price: Double,
    @SerializedName("unit") val unit: String,
    @SerializedName("market_name") val marketName: String,
    @SerializedName("reference_period_end") val date: String
)
