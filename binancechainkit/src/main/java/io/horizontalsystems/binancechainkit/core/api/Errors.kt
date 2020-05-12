package io.horizontalsystems.binancechainkit.core.api

import com.google.gson.annotations.SerializedName


sealed class BinanceException(message: String) : IllegalArgumentException(message) {

    class InvalidType(errorMessage: String) :
        BinanceException("Invalid Type:$errorMessage" )
}

class BinanceError: Exception() {
    var code: Int = 0

    @SerializedName("message")
    override var message: String = ""

    fun description(): String{
        var text = ""
        if (code > 0){
            text += "Code: $code,"
        }
        text += " $message"
        return text
    }
}