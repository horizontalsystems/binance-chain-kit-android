package io.horizontalsystems.binancechainkit.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity
class Transaction(

    @SerializedName("txHash")
    @PrimaryKey
    val transactionId: String,

    @SerializedName("blockHeight")
    val blockNumber: Int,

    @SerializedName( "timeStamp")
    val blockTime: Date,

    @SerializedName("fromAddr")
    val from: String,

    @SerializedName("toAddr")
    val to: String,

    @SerializedName("value")
    val amount: String,

    @SerializedName("txAsset")
    val symbol: String,

    @SerializedName("txFee")
    val fee: String,

    val memo: String?
)
