package io.horizontalsystems.binancechainkit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.binancechainkit.models.Transaction

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(transactions: List<Transaction>)

    @Query("SELECT * FROM `Transaction` WHERE symbol = :symbol ORDER BY blockTime DESC")
    fun getAll(symbol: String): List<Transaction>

    @Query("DELETE FROM `Transaction`")
    fun deleteAll()
}
