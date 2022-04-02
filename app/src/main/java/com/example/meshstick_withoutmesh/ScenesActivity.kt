package com.example.meshstick_withoutmesh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adaptors.RVSceneAdaptor
import com.example.meshstick_withoutmesh.types.Scene
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R

class ScenesActivity : AppCompatActivity() {

    private lateinit var adaptor: RVSceneAdaptor

    //Обработка результатов других activity
    val sceneLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            when (result.resultCode) {
                //Обновляем данные изменённые в LampsActivity
                1 -> {
                    val pos = result.data!!.getIntExtra("pos", 0)
                    adaptor.notifyItemChanged(pos)
                }
                //добавляем выбранные сцены из SavedSceneActivity
                2 -> {
                    scenes.add(result.data!!.getParcelableExtra<Scene>("scene")!!)

                    adaptor.notifyItemInserted(scenes.size - 1)
                }
                else -> Log.d("RES_CODE", "${result.resultCode}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scenes)

        val sceneRV = findViewById<RecyclerView>(R.id.rl_scenes)
        sceneRV.layoutManager = LinearLayoutManager(this)
        adaptor = RVSceneAdaptor(scenes, this)
        sceneRV.adapter = adaptor

    }

    //Хот-бар
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_scene, menu)
        return true
    }

    //Обработка кнопок хот-бара
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //Добавить сцену
            R.id.action_add_scene -> {
                addScene()
                return true
            }
            //Перейти в SavedScenesActivity
            R.id.action_savedScenes -> {
                sceneLauncher.launch(Intent(this, SavedScenesActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Добавление сцены
    private fun addScene() {
        adaptor.add(Scene())
    }

}