package io.horizontalsystems.binancechainkit.storage

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import io.horizontalsystems.binancechainkit.models.Transaction

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(transactions: List<Transaction>)

    @Query("SELECT * FROM `Transaction` WHERE symbol = :symbol ORDER BY blockTime DESC")
    fun getAll(symbol: String): List<Transaction>

    @Query("DELETE FROM `Transaction`")
    fun deleteAll()

    @Query("SELECT * FROM `Transaction` WHERE transactionId = :id LIMIT 1")
    fun getById(id: String) : Transaction?

    @RawQuery
    fun getSql(query: SupportSQLiteQuery): List<Transaction>
}
