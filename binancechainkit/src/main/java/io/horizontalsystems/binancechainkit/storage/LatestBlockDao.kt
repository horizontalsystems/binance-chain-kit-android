package io.horizontalsystems.binancechainkit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.binancechainkit.models.LatestBlock

@Dao
interface LatestBlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(latestBlock: LatestBlock)

    @Query("SELECT * FROM LatestBlock LIMIT 1")
    fun get(): LatestBlock?

    @Query("DELETE FROM LatestBlock")
    fun deleteAll()
}
