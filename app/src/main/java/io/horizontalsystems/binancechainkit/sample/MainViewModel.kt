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
        val words = "error sound chuckle illness reveal echo close lock buddy large cook apple saddle rural trouble matter pluck inner window need sphere census smooth sun".split(" ")
        val passphrase = ""

        binanceChainKit = BinanceChainKit.instance(App.instance, words, passphrase, "MyBinanceWallet", BinanceChainKit.NetworkType.TestNet)
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

            adapter.latestBlockFlowable.subscribe {
                latestBlock.postValue(binanceChainKit.latestBlock)
            }
        }

        binanceChainKit.refresh()
    }

    private fun updateBalance(adapter: BinanceAdapter) {
        balance.postValue("${adapter.balance} ${adapter.name}")
    }
}
