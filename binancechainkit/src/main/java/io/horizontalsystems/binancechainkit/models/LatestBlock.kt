package io.horizontalsystems.binancechainkit.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LatestBlock(val height: Int, val hash: String, val time: String) {

    @PrimaryKey
    var id: String = "latest-block"
}
