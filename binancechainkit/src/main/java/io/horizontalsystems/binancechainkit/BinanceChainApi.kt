package io.horizontalsystems.binancechainkit

import com.binance.dex.api.client.BinanceDexEnvironment
import com.binance.dex.api.client.Wallet
import com.binance.dex.api.client.domain.TransactionMetadata
import com.binance.dex.api.client.domain.broadcast.TransactionOption
import com.binance.dex.api.client.domain.broadcast.Transfer
import com.binance.dex.api.client.encoding.message.TransactionRequestAssembler
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.horizontalsystems.binancechainkit.models.Transaction
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.math.BigDecimal

class BinanceChainApi(words: List<String>, networkType: BinanceChainKit.NetworkType) {

    val address: String

    private val wallet: Wallet
    private var binanceChainApiService: BinanceChainApiService

    init {
        val binanceEnv = when (networkType) {
            BinanceChainKit.NetworkType.MainNet -> BinanceDexEnvironment.PROD
            BinanceChainKit.NetworkType.TestNet -> BinanceDexEnvironment.TEST_NET
        }

        wallet = Wallet.createWalletFromMnemonicCode(words, binanceEnv)
        address = wallet.address

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(binanceEnv.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        binanceChainApiService = retrofit.create(BinanceChainApiService::class.java)
    }

    fun getBalances(account: String): Single<List<Balance>> {
        return binanceChainApiService.account(account)
            .map { it.balances }
            .onErrorResumeNext {
                if (it is HttpException && it.code() == 404) {
                    Single.just(listOf())
                } else {
                    Single.error(it)
                }
            }
    }

    fun getLatestBlock(): Single<LatestBlock> {
        return binanceChainApiService.nodeInfo()
            .map { LatestBlock(it.sync_info.blockHeight, it.sync_info.blockHash, it.sync_info.blockTime) }
    }

    fun getTransactions(account: String, startTime: Long): Single<List<Transaction>> {
        return binanceChainApiService.transactions(account, startTime)
            .map { it.tx }
    }

    fun send(symbol: String, to: String, amount: BigDecimal, memo: String): Single<String> {
        return binanceChainApiService.nodeInfo()
            .flatMap {
                wallet.chainId = it.node_info.network

                binanceChainApiService.account(wallet.address)
            }
            .flatMap { account ->
                wallet.accountNumber = account.accountNumber
                wallet.sequence = account.sequence
                val sync = true

                val transfer = Transfer()
                transfer.coin = symbol
                transfer.fromAddress = wallet.getAddress()
                transfer.toAddress = to
                transfer.amount = amount.toPlainString()

                val options = TransactionOption(memo, 1, null)

                val assembler = TransactionRequestAssembler(wallet, options)
                val requestBody = assembler.buildTransfer(transfer)

                binanceChainApiService.broadcast(sync, requestBody)
            }
            .map {
                it.first().hash
            }
    }

}

interface BinanceChainApiService {

    @GET("/api/v1/account/{address}")
    fun account(@Path("address") address: String): Single<BinanceResponse.Account>

    @GET("/api/v1/node-info")
    fun nodeInfo(): Single<BinanceResponse.NodeInfoWrapper>

    @GET("/api/v1/transactions")
    fun transactions(
        @Query("address") address: String,
        @Query("startTime") startTime: Long,
        @Query("txType") txType: String = "TRANSFER",
        @Query("limit") limit: Int = 1000
    ): Single<BinanceResponse.Transactions>

    @POST("api/v1/broadcast")
    fun broadcast(@Query("sync") sync: Boolean, @Body transaction: RequestBody): Single<List<TransactionMetadata>>
}

class BinanceResponse {

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
