package io.horizontalsystems.binancechainkit.core

import io.horizontalsystems.binancechainkit.models.TransactionInfo
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class Asset(val symbol: String) {

    var balance: BigDecimal = BigDecimal(0)
        set(value) {
            field = value
            balanceSubject.onNext(balance)
        }

    val balanceFlowable: Flowable<BigDecimal>
        get() = balanceSubject.toFlowable(BackpressureStrategy.BUFFER)

    val transactionsFlowable: Flowable<List<TransactionInfo>>
        get() = transactionsSubject.toFlowable(BackpressureStrategy.BUFFER)

    val transactionsSubject = PublishSubject.create<List<TransactionInfo>>()

    private val balanceSubject = PublishSubject.create<BigDecimal>()

}
