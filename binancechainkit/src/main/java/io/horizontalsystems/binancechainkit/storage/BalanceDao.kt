package io.horizontalsystems.binancechainkit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.binancechainkit.models.Balance

@Dao
interface BalanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(balances: List<Balance>)

    @Query("SELECT * FROM Balance WHERE symbol = :symbol LIMIT 1")
    fun getBalance(symbol: String): Balance?

    @Query("DELETE FROM Balance")
    fun deleteAll()

}
