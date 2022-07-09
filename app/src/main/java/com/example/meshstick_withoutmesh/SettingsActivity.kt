package com.example.meshstick_withoutmesh

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import com.example.meshstick_withoutmesh.types.Colored
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.SceneComponents
import com.example.meshstick_withoutmesh.types.scenes
import com.example.meshstick_withoutmesh.viewmodels.SettingVMFactory
import com.example.meshstick_withoutmesh.viewmodels.SettingsVM
import com.example.myapplication.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var vm: SettingsVM

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
        val colorLayout: LinearLayout = findViewById(R.id.ll_showColor)
        val btSave: Button = findViewById(R.id.bt_save)
        val image: ImageView = findViewById(R.id.iv_color)

        val groupPosition = this.intent.getIntExtra("group_position", -1)
        val scenePosition = this.intent.getIntExtra("scene_position", -1)

        if (groupPosition != -1) {
            sbRed.progress = (scenes[scenePosition].sceneComponents[groupPosition] as Group).red
            sbGreen.progress = (scenes[scenePosition].sceneComponents[groupPosition] as Group).green
            sbBlue.progress = (scenes[scenePosition].sceneComponents[groupPosition] as Group).blue
            val drawablePic = ContextCompat.getDrawable(this, R.drawable.ic_sphere_top_layer)
            DrawableCompat.setTint(drawablePic!!, Color.rgb(sbRed.progress, sbGreen.progress, sbBlue.progress))
            image.setImageDrawable(drawablePic)
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
            val drawablePic = ContextCompat.getDrawable(this, R.drawable.ic_sphere_top_layer)
            DrawableCompat.setTint(drawablePic!!, Color.rgb(sbRed.progress, sbGreen.progress, sbBlue.progress))
            image.setImageDrawable(drawablePic)
            vm = ViewModelProvider(
                this,
                SettingVMFactory(sbRed.progress, sbGreen.progress, sbBlue.progress)
            )[SettingsVM::class.java]
            vm.result.observe(this) { color ->
                val drawablePic = ContextCompat.getDrawable(this, R.drawable.ic_sphere_top_layer)
                DrawableCompat.setTint(drawablePic!!, color)
                image.setImageDrawable(drawablePic)
            }
        }
        //Обработка взаимодействия с seekBar Red
        sbRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                vm.saveColor(red = sbRed.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //Обработка взаимодействия с seekBar Green
        sbGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                vm.saveColor(green = sbGreen.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //Обработка взаимодействия с seekBar Blue
        sbBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                vm.saveColor(blue = sbBlue.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btSave.setOnClickListener {
            saveChanges(
                sceneComponent,
                lampName.text.toString(),
                sbRed.progress,
                sbGreen.progress,
                sbBlue.progress,
                groupPosition
            )
        }

    }

    private fun saveChanges(
        sceneComponent: SceneComponents,
        name: String,
        red: Int,
        green: Int,
        blue: Int,
        groupPosition: Int
    ) {
        val intent = Intent()

        sceneComponent.name = name
        if (sceneComponent is Colored) {
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
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val scenePosition = this.intent.getIntExtra("scene_position", -1)
        val componentPosition = this.intent.getIntExtra("component_position", -1)

        // если лампа находится в группе
        val groupPosition = this.intent.getIntExtra("group_position", -1)
        if (groupPosition != -1) {
            supportActionBar?.title = (scenes[scenePosition].sceneComponents[groupPosition] as Group)
                .lamps[componentPosition].name
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