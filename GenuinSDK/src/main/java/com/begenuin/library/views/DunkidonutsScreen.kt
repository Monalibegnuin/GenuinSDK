package com.begenuin.library.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.begenuin.library.R
import com.google.android.material.button.MaterialButton

class DunkidonutsScreen : AppCompatActivity() {

    lateinit var communityClick: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dunkidonuts_screen)

        communityClick = findViewById(R.id.btnCommunity)
        communityClick.setOnClickListener{
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
        }
    }
}