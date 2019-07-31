package io.horizontalsystems.binancechainkit.sample

import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.binancechainkit.models.TransactionInfo

class TransactionsFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var transactionHeader: LinearLayout

    private val transactionsAdapter = TransactionsAdapter()
    private var selectedCoin = "BNB"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)

            viewModel.transactions.observe(this, Observer { mapped ->
                mapped?.let {
                    val list = mapped[selectedCoin]
                    if (list != null) {
                        transactionsAdapter.items = list
                        transactionsAdapter.notifyDataSetChanged()
                    }
                }
            })

            viewModel.latestBlock.observe(this, Observer { block ->
                transactionsAdapter.lib = block.height
            })

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transactions, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionsRecyclerView = view.findViewById(R.id.transactions)
        transactionsRecyclerView.adapter = transactionsAdapter
        transactionsRecyclerView.layoutManager = LinearLayoutManager(context)

        transactionHeader = view.findViewById(R.id.transactionHeader)

        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        viewModel.adapters.forEach { adapter ->
            val tv = TextView(context)
            tv.layoutParams = layoutParams
            tv.text = adapter.name
            tv.setPadding(0, 0, 25, 0)
            tv.setTextColor(Color.parseColor("#ff33b5e5"))
            tv.setOnClickListener {
                selectedCoin = adapter.name
                viewModel.updateTransactions(adapter)
            }

            transactionHeader.addView(tv)
        }
    }
}

class TransactionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items = listOf<TransactionInfo>()
    var lib: Int = 0

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderTransaction(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_transaction, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderTransaction -> holder.bind(items[position], itemCount - position, lib)
        }
    }
}

class ViewHolderTransaction(private val containerView: View) : RecyclerView.ViewHolder(containerView) {
    private val summary = containerView.findViewById<TextView>(R.id.summary)!!

    fun bind(tx: TransactionInfo, index: Int, lib: Int) {
        containerView.setBackgroundColor(if (index % 2 == 0)
            Color.parseColor("#dddddd") else
            Color.TRANSPARENT
        )

        val status = if (tx.blockNumber < lib) {
            "Confirmed"
        } else {
            "${tx.blockNumber - lib} blocks to be confirmed"
        }

        val value = """
            - #$index
            - ID: ${tx.hash}
            - Status: $status
            - From: ${tx.from}
            - To: ${tx.to}
            - Amount: ${tx.amount} ${tx.symbol}
            - Time: ${tx.date}
            - Memo: ${tx.memo}
        """

        summary.text = value.trimIndent()
    }
}
