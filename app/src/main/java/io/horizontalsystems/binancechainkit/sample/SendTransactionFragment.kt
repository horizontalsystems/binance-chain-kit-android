package io.horizontalsystems.binancechainkit.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SendTransactionFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var sendButton: Button
    private lateinit var sendAmount: EditText
    private lateinit var sendMemo: EditText
    private lateinit var sendAddress: EditText

    private var adapter: BinanceAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
            adapter = viewModel.adapters.first()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_send_receive, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendAddress = view.findViewById(R.id.sendUsername)
        sendAmount = view.findViewById(R.id.sendAmount)
        sendMemo = view.findViewById(R.id.sendMemo)
        sendButton = view.findViewById(R.id.sendButton)
        sendButton.setOnClickListener {
            when {
                sendAddress.text.isEmpty() -> sendAddress.error = "Send address cannot be blank"
                sendAmount.text.isEmpty() -> sendAmount.error = "Send amount cannot be blank"
                else -> send()
            }
        }
        sendAddress.setText("tbnb1whutmq6c6c8ky3hmw4pzpxwnf9z5plj7u7yeam")
        sendAmount.setText("2")
        sendMemo.setText("memo1")
    }

    private fun send() {
        adapter?.let { adapter ->
            adapter.send(sendAddress.text.toString(), sendAmount.text.toString().toBigDecimal(), sendMemo.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { e -> e.printStackTrace() }
                    .subscribe({
                        messageSent(null)
                    }, {
                        messageSent(it)
                    })

            sendAddress.text = null
            sendAmount.text = null
            sendMemo.text = null
        }
    }

    private fun messageSent(sendError: Throwable?) {
        val message = if (sendError != null) {
            sendError.localizedMessage
        } else {
            " Successfully sent!"
        }

        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        }
    }

}
