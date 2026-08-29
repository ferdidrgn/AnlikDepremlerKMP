package com.ferdidrgn.anlikdepremler.data.remote.dto

@Serializable
data class HomeSliderDto(
    val image: String? = null,
    val title: String? = null,
    val description: String? = null,
    val link: String? = null
)
