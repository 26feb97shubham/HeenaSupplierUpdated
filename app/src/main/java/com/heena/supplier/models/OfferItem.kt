package com.heena.supplier.models

import java.io.Serializable

data class OfferItem(
    val price: String? = null,
    val service_id: Int? = null,
    val name: String? = null,
    val gallery: List<String?>? = null,
    val offer_id: Int? = null,
    val offer_price: String? = null
): Serializable
