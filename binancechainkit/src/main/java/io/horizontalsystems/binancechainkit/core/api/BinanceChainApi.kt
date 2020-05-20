package io.horizontalsystems.binancechainkit.core.api


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import io.horizontalsystems.binancechainkit.BinanceChainKit
import io.horizontalsystems.binancechainkit.core.GsonUTCDateAdapter
import io.horizontalsystems.binancechainkit.core.Wallet
import io.horizontalsystems.binancechainkit.core.retryWithDelay
import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.horizontalsystems.binancechainkit.models.Transaction
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.math.BigDecimal
import java.util.*

class BinanceChainApi(networkType: BinanceChainKit.NetworkType) {


    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private var binanceChainApiService: BinanceChainApiService

    init {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logger)

        val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, GsonUTCDateAdapter())
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(networkType.endpoint)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient.build())
            .build()

        binanceChainApiService = retrofit.create(BinanceChainApiService::class.java)
    }

    fun getBalances(account: String): Single<List<Balance>> {
        return binanceChainApiService.account(account)
            .map { it.balances }
            .onErrorResumeNext {
                Single.error(parseError(it))
            }
            .retryWithDelay(1)
    }

    fun getLatestBlock(): Single<LatestBlock> {
        return binanceChainApiService.nodeInfo()
            .map {
                LatestBlock(
                    it.sync_info.blockHeight,
                    it.sync_info.blockHash,
                    it.sync_info.blockTime
                )
            }
            .onErrorResumeNext {
                Single.error(parseError(it))
            }
            .retryWithDelay(1)
    }

    fun getTransactions(account: String, startTime: Long): Single<List<Transaction>> {
        return binanceChainApiService.transactions(account, startTime)
            .map { it.tx }
            .onErrorResumeNext {
                Single.error(parseError(it))
            }
            .retryWithDelay(1)
    }

    fun send(symbol: String, to: String, amount: BigDecimal, memo: String, wallet: Wallet): Single<String> {

        return binanceChainApiService.nodeInfo()
            .flatMap {
                wallet.chainId = it.node_info.network

                binanceChainApiService.account(wallet.address)
            }
            .flatMap { account ->
                wallet.accountNumber = account.accountNumber
                wallet.sequence = account.sequence

                val sync = true
                val message = Message.transfer(symbol, amount, to, memo, wallet)

                binanceChainApiService.broadcast(sync, message.buildTransfer())
            }
            .map {
                it.first().hash
            }
            .onErrorResumeNext {
                Single.error(parseError(it))
            }
    }

    private fun parseError(it: Throwable): Throwable {
        return if (it is HttpException) {
            val adapter: TypeAdapter<BinanceError> = Gson().getAdapter(BinanceError::class.java)
            val binanceError: BinanceError = adapter.fromJson(it.response()?.errorBody()?.string())
            binanceError.code = it.code()
            binanceError
        } else {
            it.fillInStackTrace()
        }
    }

}

interface BinanceChainApiService {

    @GET("/api/v1/account/{address}")
    fun account(@Path("address") address: String): Single<Response.Account>

    @GET("/api/v1/node-info")
    fun nodeInfo(): Single<Response.NodeInfoWrapper>

    @GET("/api/v1/transactions")
    fun transactions(
        @Query("address") address: String,
        @Query("startTime") startTime: Long,
        @Query("txType") txType: String = "TRANSFER",
        @Query("limit") limit: Int = 1000
    ): Single<Response.Transactions>

    @POST("api/v1/broadcast")
    fun broadcast(@Query("sync") sync: Boolean, @Body transaction: RequestBody): Single<List<TransactionMetadata>>
}
