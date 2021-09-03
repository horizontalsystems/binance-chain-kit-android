package io.horizontalsystems.binancechainkit.core

import io.horizontalsystems.binancechainkit.models.TransactionFilterType
import io.horizontalsystems.binancechainkit.models.TransactionInfo
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class Asset(val symbol: String, private val account: String) {

    var balance: BigDecimal = BigDecimal(0)
        set(value) {
            field = value
            balanceSubject.onNext(balance)
        }

    val balanceFlowable: Flowable<BigDecimal>
        get() = balanceSubject.toFlowable(BackpressureStrategy.BUFFER)

    fun getTransactionsFlowable(filterType: TransactionFilterType? = null): Flowable<List<TransactionInfo>> {
        val observable = when (filterType) {
            null -> {
                transactionsSubject
            }
            TransactionFilterType.Incoming -> {
                transactionsSubject
                    .map { it.filter { it.to == account } }
                    .filter { it.isNotEmpty() }
            }
            TransactionFilterType.Outgoing -> {
                transactionsSubject
                    .map { it.filter { it.from == account } }
                    .filter { it.isNotEmpty() }
            }
        }

        return observable.toFlowable(BackpressureStrategy.BUFFER)
    }

    val transactionsSubject = PublishSubject.create<List<TransactionInfo>>()

    private val balanceSubject = PublishSubject.create<BigDecimal>()

}
