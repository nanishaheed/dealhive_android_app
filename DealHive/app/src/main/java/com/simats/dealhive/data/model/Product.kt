package com.simats.dealhive.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("source")
    val source: String, // "amazon" or "flipkart"
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("price")
    val price: Int,
    
    @SerializedName("original_price", alternate = ["originalPrice"])
    val originalPrice: Int? = null,
    
    @SerializedName("discount")
    val discount: Int? = null,
    
    @SerializedName("rating")
    val rating: Float,
    
    @SerializedName("reviews")
    val reviews: String,
    
    @SerializedName("image")
    val image: String,
    
    @SerializedName("specs")
    val specs: Map<String, String>? = null,
    
    @SerializedName("key_features")
    val keyFeatures: List<KeyFeature>? = null,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("ar_model")
    val arModel: String? = null,
    
    @SerializedName("offers")
    val offers: List<Offer>? = null,
    
    @SerializedName("category_id")
    val categoryId: Int? = null
)

data class Offer(
    @SerializedName("type")
    val type: String, // "bank", "exchange", "coupon", "emi", "delivery", "combo", "other"
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("icon")
    val icon: String? = null
)

data class KeyFeature(
    @SerializedName("icon")
    val icon: String,
    
    @SerializedName("label")
    val label: String
)

data class Comparison(
    val id: String,
    val title: String,
    val count: String,
    val img1: String,
    val img2: String
)
