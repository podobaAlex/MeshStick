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
import com.example.meshstick_withoutmesh.adaptors.RVLampAdaptor
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R

class LampsActivity : AppCompatActivity() {

    private lateinit var adaptor: RVLampAdaptor
    private var num: Int = 0

    val lampsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            when (result.resultCode) {
                1 -> {
                    adaptor.changeData(
                        result.data!!.getStringExtra("name") ?: "null",
                        result.data!!.getIntExtra("position_comeback", 1),
                        result.data!!.getStringExtra("red")!!.toInt(),
                        result.data!!.getStringExtra("green")!!.toInt(),
                        result.data!!.getStringExtra("blue")!!.toInt()
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

        adaptor = RVLampAdaptor(scenes[num].lamps, this)
        recyclerView.adapter = adaptor

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    override fun onBackPressed() {
        intent = Intent(this, ScenesActivity::class.java)
        intent.putExtra("pos", num)
        setResult(1, intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar?.title = scenes[num].getName()
        menuInflater.inflate(R.menu.menu_lamps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                adaptor.addLamp(Lamp("lamp"))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

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
