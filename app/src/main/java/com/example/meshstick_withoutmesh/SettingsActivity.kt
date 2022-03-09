package com.example.meshstick_withoutmesh

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.meshstick_withoutmesh.types.*
import com.example.myapplication.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // кнопка "назад" в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Передача начальных значений объектам LampSettingsActivity
        val lampName: EditText = findViewById(R.id.et_lampName)
        val sbRed: SeekBar = findViewById(R.id.sb_red)
        val sbGreen: SeekBar = findViewById(R.id.sb_green)
        val sbBlue: SeekBar = findViewById(R.id.sb_blue)
        val color: LinearLayout = findViewById(R.id.ll_showColor)

        val groupPosition = this.intent.getIntExtra("group_position", -1)
        val scenePosition = this.intent.getIntExtra("scene_position", -1)

        if (groupPosition != -1) {
            sbRed.progress = (scenes[scenePosition].sceneComponents[groupPosition] as Group).red
            sbGreen.progress = (scenes[scenePosition].sceneComponents[groupPosition] as Group).green
            sbBlue.progress = (scenes[scenePosition].sceneComponents[groupPosition] as Group).blue
            sbRed.visibility = View.GONE
            sbGreen.visibility = View.GONE
            sbBlue.visibility = View.GONE
            findViewById<TextView>(R.id.tv_red).visibility = View.GONE
            findViewById<TextView>(R.id.tv_green).visibility = View.GONE
            findViewById<TextView>(R.id.tv_blue).visibility = View.GONE
        }

        val sceneComponent = this.intent.getParcelableExtra<SceneComponents>("component")!!
        lampName.setText(sceneComponent.name)
        if (sceneComponent is Colored) {
            sbRed.progress = sceneComponent.red
            sbGreen.progress = sceneComponent.green
            sbBlue.progress = sceneComponent.blue
        }

        var red = sbRed.progress
        var green = sbGreen.progress
        var blue = sbBlue.progress


        color.setBackgroundColor(Color.rgb(red, green, blue))

        val activityIntent = this.intent

        //Обработка взаимодействия с editText lampName
        lampName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                sceneComponent.name = lampName.text.toString()
                val intent = Intent()
                intent.putExtra("component", sceneComponent)
                intent.putExtra("position_comeback", activityIntent.getIntExtra("position_settings", 0))

                setResult(1, intent)
            }

        })

        //Обработка взаимодействия с seekBar Red
        sbRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                red = sbRed.progress
                color.setBackgroundColor(Color.rgb(red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveChanges(sceneComponent, red, green, blue, groupPosition)
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
                saveChanges(sceneComponent, red, green, blue, groupPosition)
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
                saveChanges(sceneComponent, red, green, blue, groupPosition)
            }

        })

//        //Сохранение изменений
//        findViewById<Button>(R.id.bt_save).setOnClickListener {
//            val intent = Intent()
//
//            sceneComponent.name = lampName.text.toString()
//            if (sceneComponent is Lamp) {
//                sceneComponent.red = red
//                sceneComponent.green = green
//                sceneComponent.blue = blue
//            }
//
//            if (sceneComponent is Group) {
//                sceneComponent.red = red
//                sceneComponent.green = green
//                sceneComponent.blue = blue
//            }
//
//            intent.putExtra("component", sceneComponent)
//            intent.putExtra("position_comeback", this.intent.getIntExtra("position_settings", 1))
//
//            if (groupPosition != -1) {
//                intent.putExtra("group_position", groupPosition)
//            }
//
//            setResult(1, intent)
//            finish()
//        }


    }

    fun saveChanges(sceneComponent: SceneComponents, red: Int, green: Int, blue: Int, groupPosition: Int) {
        val intent = Intent()
        val lampName: EditText = findViewById(R.id.et_lampName)

        sceneComponent.name = lampName.text.toString()
        if (sceneComponent is Lamp) {
            sceneComponent.red = red
            sceneComponent.green = green
            sceneComponent.blue = blue
        }

        if (sceneComponent is Group) {
            sceneComponent.red = red
            sceneComponent.green = green
            sceneComponent.blue = blue
        }


        intent.putExtra("component", sceneComponent)
        intent.putExtra("position_comeback", this.intent.getIntExtra("component_position", 1))

        if (groupPosition != -1) {
            intent.putExtra("group_position", groupPosition)
        }

        setResult(1, intent)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val scenePosition = this.intent.getIntExtra("scene_position", -1)
        val componentPosition = this.intent.getIntExtra("component_position", -1)

        // если лампа находится в группе
        val groupPosition = this.intent.getIntExtra("group_position", -1)
        if (groupPosition != -1) {
            supportActionBar?.title = (scenes[scenePosition].sceneComponents[groupPosition] as Group).name

        } else {
            supportActionBar?.title = scenes[scenePosition].sceneComponents[componentPosition].name
        }
        return true
    }

    //Обработка кнопок хот-бара
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}