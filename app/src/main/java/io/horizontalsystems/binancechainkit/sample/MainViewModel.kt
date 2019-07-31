package io.horizontalsystems.binancechainkit.sample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.binancechainkit.BinanceChainKit
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.horizontalsystems.binancechainkit.models.TransactionInfo
import io.reactivex.disposables.CompositeDisposable

class MainViewModel : ViewModel() {

    val adapters = mutableListOf<BinanceAdapter>()

    val syncState = MutableLiveData<BinanceChainKit.SyncState>()
    val balance = MutableLiveData<String>()
    val transactions = MutableLiveData<Map<String, List<TransactionInfo>>>()
    val latestBlock = MutableLiveData<LatestBlock>()

    private val disposables = CompositeDisposable()

    private lateinit var binanceChainKit: BinanceChainKit

    init {
        init()
    }

    fun refresh() {
        binanceChainKit.refresh()
    }

    fun clear() {
        init()
    }

    fun updateTransactions(adapter: BinanceAdapter) {
        adapter.transactions()
            .subscribe { list -> transactions.postValue(mapOf(adapter.name to list)) }
            .let { disposables.add(it) }
    }

    // Private

    private fun init() {
        binanceChainKit = BinanceChainKit.instance(App.instance, "tbnb1sstqwv2lh26pe3xaw99mr2c4cyk2lrjx5ahkad", BinanceChainKit.NetworkType.TestNet)
        adapters.add(BinanceAdapter(binanceChainKit, "BNB"))
        adapters.add(BinanceAdapter(binanceChainKit, "ZCB-F00"))

        adapters.forEach { adapter ->

            updateBalance(adapter)
            updateTransactions(adapter)

            adapter.balanceFlowable.subscribe {
                balance.postValue("${adapter.balance} ${adapter.name}")
            }

            adapter.syncStateFlowable.subscribe {
                syncState.postValue(adapter.syncState)
            }

            adapter.transactionsFlowable.subscribe {
                updateTransactions(adapter)
            }

            adapter.irreversibleBlockFlowable.subscribe {
                latestBlock.postValue(binanceChainKit.latestBlock)
            }
        }

        binanceChainKit.refresh()
    }

    private fun updateBalance(adapter: BinanceAdapter) {
        balance.postValue("${adapter.balance} ${adapter.name}")
    }
}
