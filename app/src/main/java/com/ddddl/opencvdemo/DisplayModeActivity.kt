package com.ddddl.opencvdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class DisplayModeActivity : AppCompatActivity() {


    var option = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_mode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.cv_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.invert -> {
                option = 1
            }
            R.id.edge -> {
                option = 2
            }
            R.id.sobel -> {
                option = 3
            }
            R.id.boxblur -> {
                option = 4
            }
            else -> {
                option = 0
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
