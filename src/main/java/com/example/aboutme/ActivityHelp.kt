package com.example.aboutme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_help.*

class ActivityHelp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the graphical layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        //sends the user back to the main activity screen
        backButton.setOnClickListener(){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}