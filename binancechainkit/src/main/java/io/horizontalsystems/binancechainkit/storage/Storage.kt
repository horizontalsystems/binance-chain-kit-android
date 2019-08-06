package io.horizontalsystems.binancechainkit.storage

import androidx.sqlite.db.SimpleSQLiteQuery
import io.horizontalsystems.binancechainkit.core.IStorage
import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.horizontalsystems.binancechainkit.models.SyncState
import io.horizontalsystems.binancechainkit.models.Transaction

class Storage(private val database: KitDatabase) : IStorage {

    override fun getAllBalances(): List<Balance>?
    {
        return database.balance.getAll()
    }

    override fun removeBalances(balances: List<Balance>)
    {
        database.balance.delete(balances.map { it.symbol })
    }

    // LatestBlock

    override var latestBlock: LatestBlock?
        get() = database.latestBlock.get()
        set(value) {
            value?.let {
                database.latestBlock.update(value)
            }
        }

    // SyncState

    override var syncState: SyncState?
        get() = database.syncState.get()
        set(value) {
            value?.let {
                database.syncState.update(value)
            }
        }

    // Balance

    override fun getBalance(symbol: String): Balance? {
        return database.balance.getBalance(symbol)
    }

    override fun setBalances(balances: List<Balance>) {
        database.balance.insertAll(balances)
    }

    // Transactions

    override fun addTransactions(transactions: List<Transaction>) {
        database.transactions.insertAll(transactions)
    }

    override fun getTransactions(symbol: String, fromTransactionHash: String?, limit: Int?): List<Transaction> {
        var sql = "SELECT * FROM `Transaction` WHERE symbol = '$symbol'"

        fromTransactionHash?.let {
            database.transactions.getById(it)?.let { transaction ->
                sql += " AND blockTime <= ${transaction.blockTime.time}"
                sql += " AND transactionId <> '${transaction.transactionId}'"
            }
        }

        sql += " ORDER BY blockTime DESC"

        limit?.let {
            sql += " LIMIT $it"
        }

        return database.transactions.getSql(SimpleSQLiteQuery(sql))
    }

}
