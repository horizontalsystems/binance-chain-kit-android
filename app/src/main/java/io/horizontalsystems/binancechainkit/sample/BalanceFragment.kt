package io.horizontalsystems.binancechainkit.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.binancechainkit.BinanceChainKit

class BalanceFragment : Fragment() {

    lateinit var viewModel: MainViewModel
    lateinit var tokensRecyclerView: RecyclerView
    lateinit var refreshButton: TextView

    private val tokensAdapter = TokensAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        tokensAdapter.items = viewModel.adapters

        viewModel.balance.observe(this, Observer { balance ->
            tokensAdapter.notifyDataSetChanged()
        })

        viewModel.syncState.observe(this, Observer {
            tokensAdapter.notifyDataSetChanged()
        })

        viewModel.latestBlock.observe(this, Observer { block ->
            tokensAdapter.latestBlockHeight = block.height
            tokensAdapter.notifyDataSetChanged()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_balance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokensRecyclerView = view.findViewById(R.id.tokensRecyclerView)
        tokensRecyclerView.adapter = tokensAdapter
        tokensRecyclerView.layoutManager = LinearLayoutManager(context)

        refreshButton = view.findViewById(R.id.refresh)
        refreshButton.setOnClickListener {
            viewModel.refresh()
        }
    }
}

class TokensAdapter : RecyclerView.Adapter<ViewHolderToken>() {
    var items = listOf<BinanceAdapter>()
    var latestBlockHeight: Int = 0

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderToken {
        return ViewHolderToken(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_token, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderToken, position: Int) {
        holder.bind(items[position], latestBlockHeight)
    }
}

class ViewHolderToken(containerView: View) : RecyclerView.ViewHolder(containerView) {
    private val tokeHeader = containerView.findViewById<TextView>(R.id.header)
    private val summaryTitle = containerView.findViewById<TextView>(R.id.summaryTitle)
    private val summaryValue = containerView.findViewById<TextView>(R.id.summaryValue)

    fun bind(adapter: BinanceAdapter, latestBlockHeight: Int) {
        val syncState = when (adapter.syncState) {
            BinanceChainKit.SyncState.Synced -> "Synced"
            BinanceChainKit.SyncState.Syncing -> "Syncing"
            is BinanceChainKit.SyncState.NotSynced -> "NotSynced"
            else -> "null"
        }

        tokeHeader.text = "${adapter.name}"

        summaryTitle.text = """
            SyncState:
            Block Height:
            Balance:
        """.trimIndent()

        summaryValue.text = """
            $syncState
            $latestBlockHeight
            ${adapter.balance} ${adapter.name}
        """.trimIndent()
    }
}
