package io.horizontalsystems.binancechainkit.managers

import io.horizontalsystems.binancechainkit.BinanceChainKit
import io.horizontalsystems.binancechainkit.core.api.BinanceChainApi
import io.horizontalsystems.binancechainkit.core.api.Response
import io.reactivex.Single

class BinanceAccountProvider(
    private val apiProvider: BinanceChainApi,
    private val networkType: BinanceChainKit.NetworkType = BinanceChainKit.NetworkType.MainNet
) {

    fun getAccountAsync(words: List<String>): Single<Response.Account> {
        val wallet = BinanceChainKit.wallet(words, networkType)
        return apiProvider.getAccountAsync(wallet.address)
    }
}