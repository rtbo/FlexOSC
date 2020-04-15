package org.rtbo.flexosc

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_dummy_surface.*
import kotlinx.android.synthetic.main.fragment_connection_dialog.*

const val CONNECTION_DIALOG_TAG = "connection_dialog"

class DummySurface : AppCompatActivity() {

    private val viewModel: DummySurfaceModel by lazy {
        ViewModelProvider(this).get(DummySurfaceModel::class.java)
    }
    private val connectionDlg: ConnectionParamsDialog by lazy {
        val fragment = ConnectionParamsDialog(viewModel)
        fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.TitleDialog)
        fragment
    }

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dlg = super.onCreateDialog(savedInstanceState)
        dlg.setTitle(R.string.connection_params)
        return dlg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_connection_dialog, container, false)

        val hostAddress = v.findViewById<EditText>(R.id.hostAddress)
        val sendPort = v.findViewById<EditText>(R.id.sendPort)
        val rcvPort = v.findViewById<EditText>(R.id.rcvPort)
        val doneBtn = v.findViewById<Button>(R.id.doneBtn)

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
