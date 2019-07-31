package io.horizontalsystems.binancechainkit.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)

            tokensAdapter.items = viewModel.adapters

            viewModel.balance.observe(this, Observer { balance ->
                tokensAdapter.notifyDataSetChanged()
            })

            viewModel.syncState.observe(this, Observer {
                tokensAdapter.notifyDataSetChanged()
            })

            viewModel.latestBlock.observe(this, Observer { block ->
                tokensAdapter.irreversibleBlockHeight = block.height
                tokensAdapter.notifyDataSetChanged()
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_balance, null)
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

class TokensAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items = listOf<BinanceAdapter>()
    var irreversibleBlockHeight: Int = 0

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderToken(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_token, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderToken -> holder.bind(items[position], irreversibleBlockHeight)
        }
    }
}

class ViewHolderToken(containerView: View) : RecyclerView.ViewHolder(containerView) {
    private val tokeHeader = containerView.findViewById<TextView>(R.id.header)
    private val summaryTitle = containerView.findViewById<TextView>(R.id.summaryTitle)
    private val summaryValue = containerView.findViewById<TextView>(R.id.summaryValue)

    fun bind(adapter: BinanceAdapter, irreversibleBlockHeight: Int) {
        val syncState = when (adapter.syncState) {
            BinanceChainKit.SyncState.Synced -> "Synced"
            BinanceChainKit.SyncState.Syncing -> "Syncing"
            BinanceChainKit.SyncState.NotSynced -> "NotSynced"
            else -> "null"
        }

        tokeHeader.text = "${adapter.name}"

        summaryTitle.text = """
            SyncState:
            Irreversible Block Height:
            Balance:
        """.trimIndent()

        summaryValue.text = """
            $syncState
            $irreversibleBlockHeight
            ${adapter.balance} ${adapter.name}
        """.trimIndent()
    }
}
