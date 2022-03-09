package com.example.meshstick_withoutmesh

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class MainActivity() : AppCompatActivity() {

    private lateinit var adaptor: RVLampAdaptor
    private var scene = Scene()

    private val save_key = "save_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView : RecyclerView = findViewById(R.id.rl_lamps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adaptor = RVLampAdaptor(scene.lamps,this)
        recyclerView.adapter = adaptor

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                addLamp()
                return true
            }
            R.id.action_back -> {
                intent = Intent(this, SceneActivity::class.java)
                intent.putExtra("scene_name", scene.getName())
                intent.putExtra("lamps_count", scene.lamps.size)
                startActivityForResult(intent, 2)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    val simpleCallback = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN
            or ItemTouchHelper.END or ItemTouchHelper.START, 0) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adaptor.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            TODO("Not yet implemented")
        }

    }

    private fun addLamp() {
        scene.lamps.add(Lamp("lamp"))
        adaptor.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data == null) return
        when (requestCode) {
            1 -> {
                scene.lamps[data.getIntExtra("position", 1)].red = data.getStringExtra("red")!!.toInt()
                scene.lamps[data.getIntExtra("position", 1)].green = data.getStringExtra("green")!!.toInt()
                scene.lamps[data.getIntExtra("position", 1)].blue = data.getStringExtra("blue")!!.toInt()
                scene.lamps[data.getIntExtra("position", 1)].setName(data.getStringExtra("name") ?: "null")
                adaptor.notifyItemChanged(data.getIntExtra("position", 1))
            }
            2 -> {
                scene = Scene()
                adaptor.newData(scene.lamps)
            }
        }
    }

}