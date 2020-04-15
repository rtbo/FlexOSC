package org.rtbo.flexosc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_dummy_surface.*
import kotlinx.android.synthetic.main.fragment_connection_dialog.*

const val CONNECTION_DIALOG_TAG = "connection_dialog"

class DummySurface : AppCompatActivity() {

    private val viewModel = ViewModelProvider(this).get(DummySurfaceModel::class.java)
    private val connectionDlg = ConnectionParamsDialog(viewModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy_surface)

        viewModel.connection.observe(this, Observer {
            connectionBtn.text = it?.params.toString()
        })

        transportStartBtn.setOnClickListener {
            viewModel.sendMessage(
                OscMessage("/transport_play")
            )
        }

        transportStopBtn.setOnClickListener {
            viewModel.sendMessage(
                OscMessage("/transport_stop")
            )
        }

        recToggleBtn.setOnClickListener {
            viewModel.sendMessage(
                OscMessage("/rec_enable_toggle")
            )
        }

        stopForgetBtn.setOnClickListener {
            viewModel.sendMessage(
                OscMessage("/stop_forget")
            )
        }

        connectionBtn.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val prev = supportFragmentManager.findFragmentByTag(CONNECTION_DIALOG_TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            connectionDlg.show(ft, CONNECTION_DIALOG_TAG)
        }
    }
}

class ConnectionParamsDialog(private val viewModel: DummySurfaceModel) :
    DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_connection_dialog, container, false)

        val params = viewModel.connection.value!!.params
        hostAddress.setText(params.address)
        sendPort.setText(params.sendPort.toString())
        rcvPort.setText(params.rcvPort.toString())

        doneBtn.setOnClickListener {
            val newParams = ConnectionParams(
                hostAddress.text.toString(),
                sendPort.text.toString().toInt(),
                rcvPort.text.toString().toInt()
            )
            viewModel.connection.value = UdpOscConnection(newParams)
            dismiss()
        }

        return v
    }
}
