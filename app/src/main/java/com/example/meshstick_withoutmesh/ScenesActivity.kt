package com.example.meshstick_withoutmesh

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adapters.RVSceneAdapter
import com.example.meshstick_withoutmesh.types.Scene
import com.example.meshstick_withoutmesh.types.SwipeGesture
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R
import com.google.android.material.snackbar.Snackbar
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

        val sceneRV: RecyclerView = findViewById(R.id.rl_scenes)
        sceneRV.layoutManager = LinearLayoutManager(this)
        adapter = RVSceneAdapter(this)
        sceneRV.adapter = adapter

        fetchScenes()

        // кнопка "назад" в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // свайп влево - удаление сцены
        val swipeGesture = object : SwipeGesture(this@ScenesActivity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    val position = viewHolder.bindingAdapterPosition
                    val deletedScene = scenes[position]
                    adapter.removeScene(position)
                    Snackbar.make(sceneRV, "Сцена удалена", Snackbar.LENGTH_LONG)
                        .setAction("Отменить") {
                            adapter.add(position, deletedScene)
                        }
                        .show()
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(sceneRV)

        val btAdd: LinearLayout = findViewById(R.id.bt_add_mesh)
        btAdd.setOnClickListener {
            addScene()
        }
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
//            R.id.action_addScene -> {
//                addScene()
//                return true
//            }
            android.R.id.home -> {
                finish()
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