package io.horizontalsystems.binancechainkit.managers

import io.horizontalsystems.binancechainkit.BinanceChainApi
import io.horizontalsystems.binancechainkit.core.IStorage
import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class BalanceManager(private val storage: IStorage, private val binanceApi: BinanceChainApi) {

    interface Listener {
        fun onSyncBalances(balances: List<Balance>, latestBlock: LatestBlock)
        fun onSyncBalanceFail()
    }

    val latestBlock: LatestBlock? get() = storage.latestBlock
    var listener: Listener? = null

    private val disposables = CompositeDisposable()

    fun getBalance(symbol: String): Balance? {
        return storage.getBalance(symbol)
    }

    fun sync(account: String) {
        getBalances(account)
            .subscribeOn(Schedulers.io())
            .subscribe({
                val balances = it.first
                val latestBlock = it.second

                storage.setBalances(balances)
                storage.latestBlock = latestBlock

                listener?.onSyncBalances(balances, latestBlock)
            }, {
                it?.printStackTrace()
                listener?.onSyncBalanceFail()
            })
            .let { disposables.add(it) }
    }

    fun stop() {
        disposables.dispose()
    }

    private fun getBalances(account: String): Single<Pair<List<Balance>, LatestBlock>> {
        val latestBlock = binanceApi.getLatestBlock()
        val balances = binanceApi.getBalances(account)
        return balances.zipWith(latestBlock, BiFunction<List<Balance>, LatestBlock, Pair<List<Balance>, LatestBlock>> { t1, t2 ->
            Pair(t1, t2)
        })
    }

}
