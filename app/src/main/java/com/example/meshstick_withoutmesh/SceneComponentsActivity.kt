package com.example.meshstick_withoutmesh

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adaptors.RVSceneComponentsAdaptor
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R

class SceneComponentsActivity : AppCompatActivity() {

    private lateinit var adaptor: RVSceneComponentsAdaptor
    private var num: Int = 0

    //Обработка результатов с других activity
    val lampsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            when (result.resultCode) {
                //Результат из LampSettingsActivity
                1 -> {
                    adaptor.changeData(
                        result.data!!.getParcelableExtra<Lamp>("lamp")!!,
                        result.data!!.getIntExtra("position_comeback", 0)
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lamps)

        val recyclerView: RecyclerView = findViewById(R.id.rl_lamps)

        recyclerView.layoutManager = LinearLayoutManager(this)
        num = this.intent.getIntExtra("num", 0)

        adaptor = RVSceneComponentsAdaptor(scenes[num].sceneComponents, this)
        recyclerView.adapter = adaptor

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    //Сохранение изменений при возвращении в ScenesActivity
    override fun onBackPressed() {
        intent = Intent(this, ScenesActivity::class.java)
        intent.putExtra("pos", num)
        setResult(1, intent)
        finish()
    }

    //Верхний хот-бар
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar?.title = scenes[num].getName()
        menuInflater.inflate(R.menu.menu_scene_components, menu)
        return true
    }

    //Обработка объектов хот-бара
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //Добавление лампы
            R.id.action_add_lamp -> {
                adaptor.addLamp(Lamp("lamp"))
                return true
            }
            //Добавление группы
            R.id.action_add_group -> {
                adaptor.addGroup(Group("Group"))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Перемещение объектов recyclerview
    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN
                or ItemTouchHelper.END or ItemTouchHelper.START, 0
    ) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adaptor.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

    }

}
