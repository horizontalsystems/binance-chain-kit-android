package io.horizontalsystems.binancechainkit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.horizontalsystems.binancechainkit.core.Asset
import io.horizontalsystems.binancechainkit.core.Wallet
import io.horizontalsystems.binancechainkit.core.api.BinanceChainApi
import io.horizontalsystems.binancechainkit.helpers.Crypto
import io.horizontalsystems.binancechainkit.managers.BalanceManager
import io.horizontalsystems.binancechainkit.managers.TransactionManager
import io.horizontalsystems.binancechainkit.models.Balance
import io.horizontalsystems.binancechainkit.models.LatestBlock
import io.horizontalsystems.binancechainkit.models.Transaction
import io.horizontalsystems.binancechainkit.models.TransactionInfo
import io.horizontalsystems.binancechainkit.storage.KitDatabase
import io.horizontalsystems.binancechainkit.storage.Storage
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.LinkedHashMap

class BinanceChainKit(
    private val account: String,
    private val balanceManager: BalanceManager,
    private val transactionManager: TransactionManager,
    private val networkType: NetworkType
) : BalanceManager.Listener, TransactionManager.Listener {

    val binanceBalance: BigDecimal
        get() = balanceManager.getBalance("BNB")?.amount ?: BigDecimal.ZERO

    var latestBlock: LatestBlock? = balanceManager.latestBlock

    val latestBlockFlowable: Flowable<LatestBlock>
        get() = latestBlockSubject.toFlowable(BackpressureStrategy.BUFFER)

    private val assets = mutableListOf<Asset>()
    private val latestBlockSubject = PublishSubject.create<LatestBlock>()

    var syncState: SyncState = SyncState.NotSynced
        set(value) {
            if (field != value) {
                field = value
                syncStateSubject.onNext(syncState)
            }
        }
    val syncStateFlowable: Flowable<SyncState>
        get() = syncStateSubject.toFlowable(BackpressureStrategy.BUFFER)
    private val syncStateSubject = PublishSubject.create<SyncState>()

    fun register(symbol: String): Asset {
        val newToken = Asset(symbol).apply {
            this.balance = balanceManager.getBalance(symbol)?.amount ?: BigDecimal(0)
        }

        assets.add(newToken)
        balanceManager.sync(account)

        return newToken
    }

    fun unregister(asset: Asset) {
        assets.removeAll { it == asset }
    }

    fun refresh() {
        if (syncState == SyncState.Syncing) return

        syncState = SyncState.Syncing

        balanceManager.sync(account)
        transactionManager.sync(account)
    }

    fun stop() {
        balanceManager.stop()
        transactionManager.stop()
    }

    fun receiveAddress(): String {
        return account
    }

    @Throws
    fun validateAddress(address: String) {
        Crypto.decodeAddress(address)
    }

    fun send(symbol: String, to: String, amount: BigDecimal, memo: String): Single<String> {
        return transactionManager
            .send(symbol, to, amount, memo)
            .doOnSuccess {
                Observable.timer(5, TimeUnit.SECONDS).subscribe {
                    refresh()
                }
            }
    }

    fun transactions(asset: Asset, fromTransactionHash: String? = null, limit: Int? = null)

            : Single<List<TransactionInfo>> {
        return transactionManager
            .getTransactions(asset.symbol, fromTransactionHash, limit)
            .map { list -> list.map { TransactionInfo(it) } }
    }

    fun statusInfo(): Map<String, Any> {
        val statusInfo = LinkedHashMap<String, Any>()

        statusInfo["Synced Until"] = latestBlock?.time?.let { parseLastBlockDate(it) } ?: "N/A"
        statusInfo["Last Block Height"] = latestBlock?.height ?: "N/A"
        statusInfo["Sync State"] = syncState.name
        statusInfo["RPC Host"] = networkType.endpoint

        return statusInfo
    }

    private fun parseLastBlockDate(date: String): Date? {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        return try {
            sdf.parse(date)
        } catch (ex: Exception) {
            null
        }
    }

    // BalanceManager Listener

    override fun onSyncBalances(balances: List<Balance>, latestBlock: LatestBlock) {
        balances.forEach { balance ->
            assetBy(balance.symbol)?.let { asset ->
                asset.balance = balance.amount
            }
        }

        this.latestBlock = latestBlock
        latestBlockSubject.onNext(latestBlock)

        syncState = SyncState.Synced
    }

    override fun onSyncBalanceFail() {
        syncState = SyncState.NotSynced
    }

    // TransactionManager Listener

    override fun onSyncTransactions(transactions: List<Transaction>) {
        transactions.groupBy { it.symbol }
            .forEach { (symbol, transactions) ->
                assetBy(symbol)?.transactionsSubject?.onNext(
                    transactions.map { TransactionInfo(it) })
            }

    }

    private fun assetBy(symbol: String): Asset? {
        return assets.find { it.symbol == symbol }
    }

    // SyncState

    enum class SyncState {
        Synced,
        NotSynced,
        Syncing
    }

    enum class NetworkType {
        MainNet,
        TestNet;

        val addressPrefix: String
            get() = when (this) {
                MainNet -> "bnb"
                TestNet -> "tbnb"
            }

        val endpoint: String
            get() = when (this) {
                MainNet -> "https://dex.binance.org"
                TestNet -> "https://testnet-dex.binance.org"
            }

    }

    companion object {

        fun instance(context: Context, words: List<String>, walletId: String,
                     networkType: NetworkType = NetworkType.MainNet): BinanceChainKit {
            val database = KitDatabase.create(context, getDatabaseName(networkType, walletId))
            val storage = Storage(database)

            val hdWallet = HDWallet(Mnemonic().toSeed(words), coinType = 714)

            val wallet = Wallet(hdWallet, networkType)

            val binanceApi = BinanceChainApi(networkType)
            val balanceManager = BalanceManager(storage, binanceApi)
            val actionManager = TransactionManager(wallet, storage, binanceApi)

            val kit = BinanceChainKit(wallet.address, balanceManager, actionManager, networkType)

            balanceManager.listener = kit
            actionManager.listener = kit


            kit.validateAddress(wallet.address)

            return kit
        }

        fun clear(context: Context, networkType: NetworkType, walletId: String) {
            SQLiteDatabase.deleteDatabase(
                context.getDatabasePath(getDatabaseName(networkType, walletId))
            )
        }

        private fun getDatabaseName(networkType: NetworkType, walletId: String): String {
            return "Binance-$networkType-$walletId"
        }
    }
}
