package io.horizontalsystems.binancechainkit.core

import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.horizontalsystems.binancechainkit.models.SyncState
import io.horizontalsystems.binancechainkit.models.Transaction

interface IStorage {
    var latestBlock: LatestBlock?
    var syncState: SyncState?

    fun setBalances(balances: List<Balance>)
    fun getBalance(symbol: String): Balance?

    fun addTransactions(transactions: List<Transaction>)
    fun getTransactions(symbol: String, fromTransactionHash: String?, limit: Int?): List<Transaction>
}
