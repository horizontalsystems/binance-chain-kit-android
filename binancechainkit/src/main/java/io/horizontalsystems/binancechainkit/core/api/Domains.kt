package io.horizontalsystems.binancechainkit.core.api

import com.google.gson.annotations.SerializedName
import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.Transaction
import org.apache.commons.lang3.builder.ToStringStyle
import org.apache.commons.lang3.builder.ToStringBuilder
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import android.R.string.ok
import io.horizontalsystems.binancechainkit.proto.Send


typealias ProtoToken = io.horizontalsystems.binancechainkit.proto.Token


class Response {

    class Account(
        @SerializedName("account_number") var accountNumber: Int, var balances: List<Balance>,
        val sequence: Long
    )

    class NodeInfoWrapper(val node_info: NodeInfo, val sync_info: SyncInfo)

    class NodeInfo(val network: String)

    class SyncInfo(
        @SerializedName("latest_block_hash")
        val blockHash: String,

        @SerializedName("latest_block_height")
        val blockHeight: Int,

        @SerializedName("latest_block_time")
        val blockTime: String
    )

    class Transactions(var tx: List<Transaction>)

}

@JsonIgnoreProperties(ignoreUnknown = true)
class TransactionMetadata(var code: Int = 0,
                          var data: String = "",
                          var hash: String = "",
                          var log: String? = "",
                          var height: Long = 0,
                          var isOk: Boolean = false) {

    override fun toString(): String {
        return ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("code", code)
            .append("data", data)
            .append("hash", hash)
            .append("log", log)
            .append("ok", ok)
            .toString()
    }

}

interface BinanceDexTransactionMessage

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
class Token(val denom: String,
            val amount: Long = 0) {

    override fun toString(): String {
        return ToStringBuilder(this, BinanceDexConstants.BINANCE_DEX_TO_STRING_STYLE)
            .append("denom", denom)
            .append("amount", amount)
            .toString()
    }

    companion object {

        fun of(source: ProtoToken): Token {
            return Token(source.denom, source.amount)
        }

        fun of(sendToken: Send.Token): Token {
            return Token(sendToken.denom, sendToken.amount)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
class InputOutput(var address: String,
                  var coins: List<Token>) {

    override fun toString(): String {
        return ToStringBuilder(this, BinanceDexConstants.BINANCE_DEX_TO_STRING_STYLE)
            .append("address", address)
            .append("coins", coins)
            .toString()
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
class TransferMessage(val inputs: List<InputOutput>,
                      val outputs: List<InputOutput>)
        :BinanceDexTransactionMessage {

    override fun toString(): String {
        return ToStringBuilder(this, BinanceDexConstants.BINANCE_DEX_TO_STRING_STYLE)
            .append("inputs", inputs)
            .append("outputs", outputs)
            .toString()
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
class SignData{

    @JsonProperty("chain_id")
    var chainId: String = ""

    @JsonProperty("account_number")
    var accountNumber: String = ""
    var sequence: String = ""
    var memo: String = ""
    lateinit var msgs: Array<BinanceDexTransactionMessage>
    var source: String = ""
    var data: ByteArray? = null

    override fun toString(): String {
        return ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("chainId", chainId)
            .append("accountNumber", accountNumber)
            .append("sequence", sequence)
            .append("memo", memo)
            .append("msgs", msgs)
            .append("source", source)
            .append("data", data)
            .toString()
    }
}



