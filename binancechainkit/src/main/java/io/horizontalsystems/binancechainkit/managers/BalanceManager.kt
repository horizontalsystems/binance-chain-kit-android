package io.horizontalsystems.binancechainkit.managers

import io.horizontalsystems.binancechainkit.core.api.BinanceChainApi
import io.horizontalsystems.binancechainkit.core.IStorage
import io.horizontalsystems.binancechainkit.core.api.BinanceError
import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

class BalanceManager(private val storage: IStorage, private val binanceApi: BinanceChainApi) {

    interface Listener {
        fun onSyncBalances(balances: List<Balance>, latestBlock: LatestBlock)
        fun onSyncBalanceFail(error: Throwable)
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

                // Sync storage info
                val balancesToSync = syncStorage( balances,latestBlock )

                listener?.onSyncBalances(balancesToSync, latestBlock)
            }, {
                it?.printStackTrace()
                listener?.onSyncBalanceFail(it)
            })
            .let { disposables.add(it) }
    }

    fun stop() {
        disposables.dispose()
    }

    private fun syncStorage(balances: List<Balance>, latestBlock: LatestBlock ) :List<Balance>
    {
        storage.latestBlock = latestBlock

        val allStoredBalances = storage.getAllBalances()
        val balancesToRemove = arrayListOf<Balance>()

        for (balance in allStoredBalances.orEmpty())
        {
            if( !balances.any{ it.symbol == balance.symbol} )
            {
                balance.amount = BigDecimal.ZERO
                balancesToRemove.add(balance)
            }
        }

        storage.setBalances(balances)
        storage.removeBalances(balancesToRemove)

        return balances + balancesToRemove
    }

    private fun getBalances(account: String): Single<Pair<List<Balance>, LatestBlock>> {
        val latestBlock = binanceApi.getLatestBlock()
        val balances = binanceApi.getBalances(account)
            .onErrorResumeNext {
                if ((it as? BinanceError)?.code == 404) {
                    //New Account
                    Single.just(listOf())
                } else {
                    Single.error(it.fillInStackTrace())
                }
            }

        return balances.zipWith(latestBlock, BiFunction<List<Balance>, LatestBlock, Pair<List<Balance>, LatestBlock>> { t1, t2 ->
            Pair(t1, t2)
        })
    }

}
