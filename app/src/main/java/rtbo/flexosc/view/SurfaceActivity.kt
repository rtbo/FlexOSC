package rtbo.flexosc.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import rtbo.flexosc.R
import rtbo.flexosc.model.OscSocketParams
import rtbo.flexosc.viewmodel.Control
import rtbo.flexosc.viewmodel.ControlSurface
import rtbo.flexosc.viewmodel.populateDummySurface

const val PARAMS_DIALOG_TAG = "params_dialog"

class SurfaceActivity : AppCompatActivity() {

    private val surface: ControlSurface by lazy {
        ViewModelProvider(this).get(ControlSurface::class.java)
    }
    private val paramsDlg: ConnectionParamsDialog by lazy {
        val fragment = ConnectionParamsDialog(surface)
        fragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.TitleDialog
        )
        fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (surface.controls.isEmpty()) {
            populateDummySurface(surface)
        }

        val controlViewFactory = SurfaceControlViewFactory(baseContext, this)

        val paramsView = TextView(baseContext)
        paramsView.text = surface.socketParams.value.toString()
        surface.socketParams.observe(this, Observer {
            paramsView.text = (it?.toString() ?: "(no connection settings)")
        })
        paramsView.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val prev = supportFragmentManager.findFragmentByTag(PARAMS_DIALOG_TAG)
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            paramsDlg.show(ft, PARAMS_DIALOG_TAG)
        }

        val layout = SurfaceLayout(baseContext, paramsView, controlViewFactory)

        for (control in surface.controls) {
            layout.addControl(control)
        }

        // we cannot afford to let such event pass
        surface.onControlAdd.observeForever(layout::addControl)
        surface.onControlRem.observeForever(layout::remControl)

        setContentView(layout)
    }
}

class ConnectionParamsDialog(private val surface: ControlSurface) : DialogFragment() {

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

        val params = surface.socketParams.value
        if (params != null) {
            hostAddress.setText(params.address)
            sendPort.setText(params.sendPort.toString())
            rcvPort.setText(params.rcvPort.toString())
        }

        doneBtn.setOnClickListener {
            surface.setSocketParams(
                OscSocketParams(
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
