package com.example.meshstick_withoutmesh

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adapters.RVSceneAdapter
import com.example.meshstick_withoutmesh.types.Scene
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R
import io.paperdb.Paper

class ScenesActivity : AppCompatActivity() {

    private lateinit var adapter: RVSceneAdapter

    //Обработка результатов других activity
    val sceneLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            when (result.resultCode) {
                //Обновляем данные изменённые в LampsActivity
                1 -> {
                    val pos = result.data!!.getIntExtra("pos", 0)
                    adapter.notifyItemChanged(pos)
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
        adapter = RVSceneAdapter(this)
        sceneRV.adapter = adapter

        fetchScenes()
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
        }
        return super.onOptionsItemSelected(item)
    }

    //Добавление сцены
    private fun addScene() {
        adapter.add(Scene())
    }

    private fun fetchScenes() {
        try {
            scenes = Paper.book().read("scenes")!!
            adapter.setData()

        } catch (e : NullPointerException) {
            Log.e("DBG_TAG", "null in fun fetchScenes")
        }
    }


}