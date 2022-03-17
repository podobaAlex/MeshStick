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
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.Scene
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R

class ScenesActivity : AppCompatActivity() {

    private lateinit var adaptor: RVSceneAdaptor

    val sceneLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            when (result.resultCode) {
                1 -> {
                    val pos = result.data!!.getIntExtra("pos", 0)
                    adaptor.notifyItemChanged(pos)
                }
                2 -> {
                    scenes.add(Scene(result.data!!.getStringExtra("scene_name") ?: "null"))

                    val size: Int = result.data!!.getIntExtra("lamps_count", 0)
                    for (i in 0 until size) {

                        scenes[scenes.size - 1].lamps.add(
                            Lamp(
                                result!!.data!!.getStringExtra("lamp${i}_name") ?: "null",
                                result.data!!.getIntExtra("lamp${i}_red", 0),
                                result.data!!.getIntExtra("lamp${i}_green", 0),
                                result.data!!.getIntExtra("lamp${i}_blue", 0)
                            )
                        )
                    }
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
            R.id.action_savedScenes -> {
                sceneLauncher.launch(Intent(this, SavedScenesActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addScene() {
        adaptor.add(Scene())
    }

}