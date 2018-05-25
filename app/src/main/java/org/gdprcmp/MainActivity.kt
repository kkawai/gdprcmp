package org.gdprcmp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.gdprcmplib.GDPRUtil
import org.gdprcmplib.GdprCmp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GDPRUtil.setGDPRInfo(this,true,null)

        GdprCmp.startCmpActivity(this)
        finish()
    }


}
