package io.horizontalsystems.binancechainkit.sample

import io.horizontalsystems.binancechainkit.BinanceChainKit
import io.horizontalsystems.binancechainkit.models.TransactionInfo
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal

class BinanceAdapter(private val binanceChainKit: BinanceChainKit, tokenSymbol: String) {

    private val asset = binanceChainKit.register(tokenSymbol)

    val name: String
        get() = asset.symbol

    val syncState: BinanceChainKit.SyncState
        get() = binanceChainKit.syncState

    val balance: BigDecimal
        get() = asset.balance

    val syncStateFlowable: Flowable<Unit>
        get() = binanceChainKit.syncStateFlowable.map { Unit }

    val balanceFlowable: Flowable<Unit>
        get() = asset.balanceFlowable.map { Unit }

    val transactionsFlowable: Flowable<Unit>
        get() = asset.getTransactionsFlowable().map { Unit }

    val latestBlockFlowable: Flowable<Unit>
        get() = binanceChainKit.latestBlockFlowable.map { Unit }

    fun send(to: String, amount: BigDecimal, memo: String): Single<String> {
        return binanceChainKit.send(asset.symbol, to, amount, memo)
    }

    fun transactions(fromTransactionHash: String? = null, limit: Int? = null): Single<List<TransactionInfo>> {
        return binanceChainKit.transactions(asset, fromTransactionHash, limit)
    }
}
