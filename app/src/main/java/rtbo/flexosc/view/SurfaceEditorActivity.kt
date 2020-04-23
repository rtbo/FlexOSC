package rtbo.flexosc.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_surface_editor.*
import rtbo.flexosc.R

class SurfaceEditorActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surface_editor)
        openMenuBut.setOnClickListener {
            drawerLayout.openDrawer(actionMenu)
        }
    }
}
