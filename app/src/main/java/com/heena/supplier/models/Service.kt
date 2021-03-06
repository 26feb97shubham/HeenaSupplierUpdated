package com.heena.supplier.models

import java.io.Serializable

data class Service(
        val price: Price? = null,
        val service_id: Int? = null,
        val subscription_id: Int? = null,
        val name: String? = null,
        val category: CategoryItem? = null,
        val gallery: List<String?>? = null,
        val address: String? = null,
        val user_id: String? = null,
        val description: String? = null,
        val lat: String? = null,
        val long: String? = null
): Serializable