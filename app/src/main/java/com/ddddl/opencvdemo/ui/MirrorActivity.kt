package com.ddddl.opencvdemo.ui

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.ddddl.opencvdemo.R
import com.ddddl.opencvdemo.view.MyPSView
import kotlinx.android.synthetic.main.activity_mirror.*

class MirrorActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mirror)

        val windowManager = windowManager
        val display = windowManager.defaultDisplay
        btn_reset.setOnClickListener(this)
        myPSView.setScreenSize(display.width, display.height)
        myPSView.setOnStepChangeListener { isEmpty ->
            btn_reset.setTextColor(if (isEmpty) Color.parseColor("#999999") else Color.parseColor("#000000"))
            btn_reset.isEnabled = !isEmpty
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_reset -> myPSView.resetView()
        }
    }
}
