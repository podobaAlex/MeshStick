package com.example.meshstick_withoutmesh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

//Начальная сцена
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Переход в ScenesActivity
        val btOpenScenes: Button = findViewById(R.id.bt_openScenes)
        btOpenScenes.setOnClickListener {
            val intent = Intent(this, ScenesActivity::class.java)
            startActivity(intent)
        }

    }

}