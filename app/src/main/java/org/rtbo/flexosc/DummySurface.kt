package org.rtbo.flexosc

import android.app.job.JobServiceEngine
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_dummy_surface.*

const val CONNECTION_DIALOG_TAG = "connection_dialog"

class DummySurface : AppCompatActivity() {

    private val viewModel = ViewModelProvider(this).get(DummySurfaceModel::class.java)
    private val connectionDlg = ConnectionDialogFragment(viewModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy_surface)

        viewModel.connection.observe(this, Observer {
            connectionBtn.text = it?.params.toString()
            connectionBtn.setIconTintResource(
                if (it == null) R.color.ledRed else R.color.ledGreen
            )
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

class ConnectionDialogFragment(private val viewModel: DummySurfaceModel) :
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
        val btn = v.findViewById<Button>(R.id.connectBtn)
        val hostAddress = v.findViewById<TextInputLayout>(R.id.hostAddress)
        val sendPort = v.findViewById<TextInputLayout>(R.id.sendPort)
        val rcvPort = v.findViewById<TextInputLayout>(R.id.rcvPort)

        btn.setOnClickListener {
            val params = ConnectionParams(
                hostAddress.editText?.text.toString(),
                sendPort.editText?.text.toString().toInt(),
                rcvPort.editText?.text.toString().toInt()
            )
            viewModel.connection.value = UdpOscConnection (params)
            dismiss()
        }

        return v
    }
}
