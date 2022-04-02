package com.example.meshstick_withoutmesh

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.meshstick_withoutmesh.types.SceneComponents
import com.example.myapplication.R

class SettingsActivity : AppCompatActivity() {
    //private var pref: SharedPreferences = getSharedPreferences("Test", MODE_PRIVATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //Передача начальных значений объектам LampSettingsActivity
        val lampName: EditText = findViewById(R.id.et_lampName)
        val sbRed: SeekBar = findViewById(R.id.sb_red)
        val sbGreen: SeekBar = findViewById(R.id.sb_green)
        val sbBlue: SeekBar = findViewById(R.id.sb_blue)
        val color: LinearLayout = findViewById(R.id.ll_showColor)

        val sceneComponent = this.intent.getParcelableExtra<SceneComponents>("component")!!
        lampName.setText(sceneComponent.name)
        sbRed.progress = sceneComponent.red
        sbGreen.progress = sceneComponent.green
        sbBlue.progress = sceneComponent.blue

        var red = sbRed.progress
        var green = sbGreen.progress
        var blue = sbBlue.progress

        color.setBackgroundColor(Color.rgb(red, green, blue))

        //Обработка взаимодействия с seekBar Red
        sbRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                red = sbRed.progress
                color.setBackgroundColor(Color.rgb(red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        //Обработка взаимодействия с seekBar Green
        sbGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                green = sbGreen.progress
                color.setBackgroundColor(Color.rgb(red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        //Обработка взаимодействия с seekBar Blue
        sbBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                blue = sbBlue.progress
                color.setBackgroundColor(Color.rgb(red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        //Сохранение изменений
        findViewById<Button>(R.id.bt_save).setOnClickListener {
            val intent = Intent()

            sceneComponent.name = lampName.text.toString()
            sceneComponent.red = red
            sceneComponent.green = green
            sceneComponent.blue = blue

            intent.putExtra("component", sceneComponent)
            intent.putExtra("position_comeback", this.intent.getIntExtra("position_settings", 1))

            setResult(1, intent)
            finish()
        }
    }
}