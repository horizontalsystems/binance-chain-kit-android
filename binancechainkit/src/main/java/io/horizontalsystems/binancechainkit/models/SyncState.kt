package io.horizontalsystems.binancechainkit.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SyncState(val transactionSyncedUntilTime: Long) {

    @PrimaryKey
    var id: String = "sync-state"
}
