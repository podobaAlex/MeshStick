package com.example.meshstick_withoutmesh

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
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
                    val position: Int = result.data!!.getIntExtra("group_position", -1)
                    if (position == -1) {
                        adaptor.changeData(
                            result.data!!.getParcelableExtra("component")!!,
                            result.data!!.getIntExtra("position_comeback", 0)
                        )
                    } else {
                        adaptor.updateLampInGroup(
                            result.data!!.getParcelableExtra("component")!!,
                            position,
                            result.data!!.getIntExtra("position_comeback", 0)
                        )
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_components)

        val btAddLamp: Button = findViewById(R.id.bt_addLamp)
        val btAddGroup: Button = findViewById(R.id.bt_addGroup)
        val recyclerView: RecyclerView = findViewById(R.id.rl_components)

        recyclerView.layoutManager = LinearLayoutManager(this)
        num = this.intent.getIntExtra("num", 0)

        adaptor = RVSceneComponentsAdaptor(scenes[num].sceneComponents, this)
        recyclerView.adapter = adaptor

        btAddLamp.setOnClickListener { adaptor.addLamp(Lamp("lamp")) }
        btAddGroup.setOnClickListener { adaptor.addGroup(Group("group")) }

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
        return true
    }

    //Перемещение объектов recyclerview
    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN
                or ItemTouchHelper.END or ItemTouchHelper.START, 0
    ) {

        var dropPosition: Int = -1
        var dy = 0f

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adaptor.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            dy = dY
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                dropPosition = when {
                    dY > 0.8f -> viewHolder.adapterPosition + 1
                    dY < -0.8f -> viewHolder.adapterPosition - 1
                    else -> viewHolder.adapterPosition
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            Log.d("DROP", "ViewHolder drag position - ${viewHolder.adapterPosition}")
            Log.d("DROP", "ViewHolder drop position - $dropPosition")
            Log.d("DROP", "dY - $dy")
            if (dropPosition >= 0 && dropPosition < adaptor.itemCount) {
                adaptor.addLampInGroup(viewHolder.adapterPosition, dropPosition)
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

    }

}
