package io.horizontalsystems.binancechainkit.core.api



sealed class BinanceException(message: String) : IllegalArgumentException(message) {

    class InvalidType(errorMessage: String) :
        BinanceException("Invalid Type:$errorMessage" )

}