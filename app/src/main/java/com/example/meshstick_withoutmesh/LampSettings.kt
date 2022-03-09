package com.example.meshstick_withoutmesh

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.myapplication.R

class LampSettings : AppCompatActivity() {
    //private var pref: SharedPreferences = getSharedPreferences("Test", MODE_PRIVATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lamp_settings)

        val lampName = findViewById<EditText>(R.id.et_lampName)
        val et_red = findViewById<EditText>(R.id.et_red)
        val et_green = findViewById<EditText>(R.id.et_green)
        val et_blue = findViewById<EditText>(R.id.et_blue)

        lampName.setText(this.intent.getStringExtra("name"))
        et_red.setText(this.intent.getStringExtra("red"))
        et_green.setText(this.intent.getStringExtra("green"))
        et_blue.setText(this.intent.getStringExtra("blue"))

        findViewById<Button>(R.id.bt_save).setOnClickListener {
            val intent = Intent()

            intent.putExtra("name", lampName.text.toString())
            intent.putExtra("red", et_red.text.toString())
            intent.putExtra("green", et_green.text.toString())
            intent.putExtra("blue", et_blue.text.toString())
            intent.putExtra("position",this.intent.getIntExtra("position",1))

            /*val editor = pref.edit()

            editor?.putString("${lampName.text.toString()}_",lampName.text.toString())
            editor?.putString("${lampName.text.toString()}_${et_red.text.toString()}_",et_red.text.toString())
            editor?.putString("${lampName.text.toString()}_${et_green.text.toString()}_",et_green.text.toString())
            editor?.putString("${lampName.text.toString()}_${et_blue.text.toString()}_",et_blue.text.toString())

            editor.apply()*/

            setResult(1, intent)
            finish()
        }
    }
}