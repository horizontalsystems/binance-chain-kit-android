package io.horizontalsystems.binancechainkit.models

import com.google.gson.annotations.SerializedName

class Bep2Token(
    val name: String,
    @SerializedName("original_symbol")val code: String,
    val symbol: String
)
