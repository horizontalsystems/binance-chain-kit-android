package io.horizontalsystems.binancechainkit.core.api

import com.google.gson.annotations.SerializedName


sealed class BinanceException(message: String) : IllegalArgumentException(message) {

    class InvalidType(errorMessage: String) :
        BinanceException("Invalid Type:$errorMessage" )
}

class BinanceError: Exception() {
    var code: String = ""

    @SerializedName("failed_tx_index")
    val failedTxIndex: String = ""

    @SerializedName("message")
    override var message: String = ""
}