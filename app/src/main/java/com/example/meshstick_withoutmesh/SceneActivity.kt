package com.example.meshstick_withoutmesh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class SceneActivity : AppCompatActivity() {

    private lateinit var adaptor: RVSceneAdaptor
    val scenes = mutableListOf<Scene>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene)

        val sceneRV = findViewById<RecyclerView>(R.id.rl_scenes)
        sceneRV.layoutManager = LinearLayoutManager(this)
        adaptor = RVSceneAdaptor(scenes, this)
        sceneRV.adapter = adaptor

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_scene, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_scene -> {
                addScene()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addScene() {
        adaptor.add(Scene())
    }

}