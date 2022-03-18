package com.example.meshstick_withoutmesh

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adaptors.RVSavedSceneAdaptor
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.Scene
import com.example.myapplication.R

class SavedScenesActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var savedScenes: MutableList<Scene>
    private lateinit var adaptor: RVSavedSceneAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_scenes)
        savedScenes = mutableListOf()
        init()

        val savedScenesRV = findViewById<RecyclerView>(R.id.rl_savedScenes)
        savedScenesRV.layoutManager = LinearLayoutManager(this)
        adaptor = RVSavedSceneAdaptor(savedScenes, this)
        savedScenesRV.adapter = adaptor
    }

    //Добавляем в сцену всё, что мы сохраняли
    private fun init() {
        pref = getSharedPreferences("Scenes", MODE_PRIVATE)
        var position = 0
        while (true) {
            if (!pref.contains("scene$position")) {
                Log.d("LOAD_DATA", "${pref.contains("scene$position")}")
                break
            }

            savedScenes.add(Scene(pref.getString("scene$position", "null")!!))
            val size = pref.getInt("scene${position}_size", 0)

            for (i in 0 until size) {
                if (pref.contains("scene${position}_lamp${i}")) {
                    savedScenes[savedScenes.size - 1].sceneComponents.add(
                        Lamp(
                            pref.getString("scene${position}_lamp${i}", "null")!!,
                            pref.getInt("scene${position}_lamp${i}_red", 0),
                            pref.getInt("scene${position}_lamp${i}_green", 0),
                            pref.getInt("scene${position}_lamp${i}_blue", 0)
                        )
                    )
                } else if (pref.contains("scene${position}_group${i}")) {

                    val groupSize = pref.getInt("scene${position}_group${i}_size", 0)
                    val group = Group(
                        pref.getString("scene${position}_group${i}", "null")!!,
                        pref.getInt("scene${position}_group${i}_red", 0),
                        pref.getInt("scene${position}_group${i}_green", 0),
                        pref.getInt("scene${position}_group${i}_blue", 0)
                    )
                    for (j in 0 until groupSize) {
                        group.lamps.add(
                            Lamp(
                                pref.getString("scene${position}_group${i}_lamp${j}", "null")!!,
                                group.red,
                                group.green,
                                group.blue
                            )
                        )
                    }
                    savedScenes[savedScenes.size - 1].sceneComponents.add(group)
                }

            }
            position++
        }

    }
}