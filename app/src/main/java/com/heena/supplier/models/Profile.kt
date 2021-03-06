package com.heena.supplier.models

import java.io.Serializable

data class Profile(
    val image: String? = null,
    val trade_license: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val country_code: String? = null,
    val user_id: Int? = null,
    val join_date: String? = null,
    val comment_avg: String? = null,
    val name: String? = null,
    val email: String? = null,
    val username: String? = null
): Serializable