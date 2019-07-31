package io.horizontalsystems.binancechainkit.models

import java.util.*

class TransactionInfo(transaction: Transaction) {
    val hash: String = transaction.transactionId
    val blockNumber: Int = transaction.blockNumber
    val date: Date = transaction.blockTime

    val from: String = transaction.from
    val to: String = transaction.to
    val amount: String = transaction.amount
    val symbol: String = transaction.symbol
    val fee: String = transaction.fee
    val memo: String? = transaction.memo
}
