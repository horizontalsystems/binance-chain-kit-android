package io.horizontalsystems.binancechainkit.core.api

import com.google.protobuf.ByteString
import io.horizontalsystems.binancechainkit.core.Wallet
import io.horizontalsystems.binancechainkit.helpers.Crypto
import io.horizontalsystems.binancechainkit.helpers.EncodeUtils
import io.horizontalsystems.binancechainkit.proto.Send
import java.math.BigDecimal
import okhttp3.RequestBody
import java.io.IOException
import java.security.NoSuchAlgorithmException
import io.horizontalsystems.binancechainkit.proto.StdSignature
import io.horizontalsystems.binancechainkit.proto.StdTx
import java.util.Collections.singletonList


class Message {

    private val MEDIA_TYPE = okhttp3.MediaType.parse("text/plain; charset=utf-8")
    private val MULTIPLY_FACTOR = BigDecimal.valueOf(1e8)
    private val MAX_NUMBER = BigDecimal(java.lang.Long.MAX_VALUE)

    private lateinit var wallet: Wallet
    private var type: MessageType = MessageType.NewOrder
    private var memo: String = ""
    private var coin: String = ""
    private var amount: BigDecimal = BigDecimal(0)
    private var toAddress: String = ""
    private var source = Source.BROADCAST


    companion object {

        @Throws(IOException::class, NoSuchAlgorithmException::class)
        fun transfer(coin: String, amount: BigDecimal, toAddress: String, memo: String = "",
                     wallet: Wallet): Message {

            val message = Message()

            message.type = MessageType.Send

            message.coin = coin
            message.amount = amount
            message.toAddress = toAddress
            message.wallet = wallet
            message.memo = memo

            return message
        }
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun buildTransfer(): RequestBody {
        return createRequestBody(buildTransferPayload())
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun buildTransferPayload(): String {

        val msgBean = createTransferMessage()
        val encData = encodeTransferMessage(msgBean)
        val encSignature = encodeSignature(sign(msgBean))
        val encStdTx = encodeStdTx( encData, encSignature)

        return EncodeUtils.bytesToHex(encStdTx)
    }

    @Throws(IOException::class)
    fun encodeStdTx(msg: ByteArray, signature: ByteArray): ByteArray {
        var stdTxBuilder = StdTx.newBuilder()
            .addMsgs(ByteString.copyFrom(msg))
            .addSignatures(ByteString.copyFrom(signature))
            .setMemo(this.memo)
            .setSource(Source.BROADCAST.value.toLong())

        return EncodeUtils.aminoWrap(stdTxBuilder.build().toByteArray(), MessageType.StdTx.typePrefixBytes, true)
    }


    @Throws(NoSuchAlgorithmException::class, IOException::class)
    private fun sign( msg: BinanceDexTransactionMessage ): ByteArray {

        val sd = SignData()
        sd.chainId = wallet.chainId
        sd.accountNumber = wallet.accountNumber.toString()
        sd.sequence = wallet?.sequence.toString()
        sd.msgs = arrayOf(msg)
        sd.memo = this.memo
        sd.source = Source.BROADCAST.value.toString()

        return wallet.sign(EncodeUtils.toJsonEncodeBytes(sd))
    }

    @Throws(IOException::class)
    fun encodeSignature(signatureBytes: ByteArray): ByteArray {
        val stdSignature = StdSignature.newBuilder().setPubKey(ByteString.copyFrom(wallet.pubKeyForSign))
            .setSignature(ByteString.copyFrom(signatureBytes))
            .setAccountNumber(wallet.accountNumber.toLong())
            .setSequence(wallet.sequence)
            .build()

        return EncodeUtils.aminoWrap(
            stdSignature.toByteArray(), MessageType.StdSignature.typePrefixBytes, false
        )
    }

    fun createTransferMessage(): TransferMessage {

        val token = Token(coin,doubleToLong(amount.toPlainString()))
        val coins = singletonList(token)

        val input = InputOutput(this.wallet.address, coins)
        val output = InputOutput(this.toAddress, coins)

        return TransferMessage(singletonList(input),singletonList(output))
    }

    private fun toProtoInput(input: InputOutput): Send.Input {
        val address = Crypto.decodeAddress(input.address)
        val builder = Send.Input.newBuilder().setAddress(ByteString.copyFrom(address))

        for (coin in input.coins) {
            val protCoin = Send.Token.newBuilder().setAmount(coin.amount)
                .setDenom(coin.denom).build()
            builder.addCoins(protCoin)
        }
        return builder.build()
    }

    private fun toProtoOutput(output: InputOutput): Send.Output {
        val address = Crypto.decodeAddress(output.address)
        val builder = Send.Output.newBuilder().setAddress(ByteString.copyFrom(address))

        for (coin in output.coins) {
            val protCoin = Send.Token.newBuilder().setAmount(coin.amount)
                .setDenom(coin.denom).build()
            builder.addCoins(protCoin)
        }
        return builder.build()
    }

    @Throws(IOException::class)
    fun encodeTransferMessage(msg: TransferMessage): ByteArray {

        val builder = Send.newBuilder()

        for (input in msg.inputs!!) {
            builder.addInputs(toProtoInput(input))
        }

        for (output in msg.outputs!!) {
            builder.addOutputs(toProtoOutput(output))
        }
        val proto = builder.build()

        return EncodeUtils.aminoWrap(proto.toByteArray(), MessageType.Send.typePrefixBytes, false)
    }


    private fun doubleToLong(d: String): Long {

        val encodeValue = BigDecimal(d).multiply(MULTIPLY_FACTOR)
        if (encodeValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw IllegalArgumentException("$d is less or equal to zero.")
        }

        if (encodeValue.compareTo(MAX_NUMBER) > 0) {
            throw IllegalArgumentException("$d is too large.")
        }
        return encodeValue.toLong()

    }

    private fun createRequestBody(payload: String): RequestBody {
        return RequestBody.create(MEDIA_TYPE, payload)
    }
}
