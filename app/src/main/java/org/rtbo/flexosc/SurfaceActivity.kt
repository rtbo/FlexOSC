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
import androidx.lifecycle.ViewModelProvider
import org.rtbo.flexosc.model.ConnectionModel
import org.rtbo.flexosc.model.ConnectionParams

class SurfaceActivity : AppCompatActivity() {

    private val model: ConnectionModel by lazy {
        ViewModelProvider(this).get(ConnectionModel::class.java)
    }
    private val connectionDlg: ConnectionParamsDialog by lazy {
        val fragment = ConnectionParamsDialog(model)
        fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.TitleDialog)
        fragment
    }

}

class ConnectionParamsDialog(private val model: ConnectionModel) : DialogFragment() {

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

        val params = model.params.value
        if (params != null) {
            hostAddress.setText(params.address)
            sendPort.setText(params.sendPort.toString())
            rcvPort.setText(params.rcvPort.toString())
        }

        doneBtn.setOnClickListener {
            model.setParams(
                ConnectionParams(
                    hostAddress.text.toString(),
                    sendPort.text.toString().toInt(),
                    rcvPort.text.toString().toInt()
                )
            )
            dismiss()
        }

        return v
    }
}
