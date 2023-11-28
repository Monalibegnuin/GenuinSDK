package com.begenuine.testfeedsdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.begenuin.library.SDKInitiate

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_test)
        SDKInitiate.loadSDK(this@MainActivity)
        finish()
    }
}