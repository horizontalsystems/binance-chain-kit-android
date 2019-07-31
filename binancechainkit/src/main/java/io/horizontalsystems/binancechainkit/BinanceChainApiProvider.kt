package io.horizontalsystems.binancechainkit

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.horizontalsystems.binancechainkit.models.Transaction
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class BinanceChainApiProvider {

    private var binanceChainApiService: BinanceChainApiService

    init {
        val baseUrl = "https://testnet-dex.binance.org"

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        binanceChainApiService = retrofit.create(BinanceChainApiService::class.java)
    }

    fun getBalances(account: String): Single<List<Balance>> {
        return binanceChainApiService.account(account)
            .map { it.balances }
    }

    fun getLatestBlock(): Single<LatestBlock> {
        return binanceChainApiService.nodeInfo()
            .map { LatestBlock(it.syncInfo.blockHeight, it.syncInfo.blockHash, it.syncInfo.blockTime) }
    }

    fun getTransactions(account: String, startTime: Long): Single<List<Transaction>> {
        return binanceChainApiService.transactions(account, startTime)
            .map { it.tx }
    }

}

interface BinanceChainApiService {

    @GET("/api/v1/account/{address}")
    fun account(@Path("address") address: String): Single<BinanceApi.Account>

    @GET("/api/v1/node-info")
    fun nodeInfo(): Single<BinanceApi.NodeInfo>

    @GET("/api/v1/transactions")
    fun transactions(
        @Query("address") address: String,
        @Query("startTime") startTime: Long,
        @Query("txType") txType: String = "TRANSFER"
    ): Single<BinanceApi.Transactions>
}

class BinanceApi {

    class Account(@SerializedName("account_number") var accountNumber: Int, var balances: List<Balance>)

    class NodeInfo(@SerializedName("sync_info") val syncInfo: SyncInfo)

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
