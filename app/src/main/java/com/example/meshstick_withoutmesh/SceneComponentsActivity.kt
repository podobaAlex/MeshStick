package com.example.meshstick_withoutmesh

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adapters.RVSceneComponentsAdapter
import com.example.meshstick_withoutmesh.fragments.SceneRenameDialogFragment
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.SwipeGesture
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R

class SceneComponentsActivity : AppCompatActivity() {

    lateinit var adapter: RVSceneComponentsAdapter
    private var num: Int = 0
    //private var  sceneComponents: MutableList<SceneComponents>? = null

    //Обработка результатов с других activity
    val lampsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            when (result.resultCode) {
                //Результат из LampSettingsActivity
                1 -> {
                    val position: Int = result.data!!.getIntExtra("group_position", -1)
                    if (position == -1) {
                        adapter.changeData(
                            result.data!!.getParcelableExtra("component")!!,
                            result.data!!.getIntExtra("position_comeback", 0)
                        )
                    } else {
                        adapter.updateLampInGroup(
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

        adapter = RVSceneComponentsAdapter(num, this)
        recyclerView.adapter = adapter

        //восстанавливаем информацию из хранилища
        fetchSceneComponents()

        btAddLamp.setOnClickListener { adapter.addLamp(Lamp("lamp")) }
        btAddGroup.setOnClickListener { adapter.addGroup(Group("group")) }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // добавляем жест свайп влево
        val swipeGesture = object : SwipeGesture(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    adapter.removeComponent(viewHolder.bindingAdapterPosition)
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(recyclerView)

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
        menuInflater.inflate(R.menu.menu_scene_components, menu)
        supportActionBar?.title = scenes[num].getName()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //Добавить сцену
            R.id.action_editSceneName -> {
                val renameSceneDialog = SceneRenameDialogFragment(scenes[num].getName(), num)
                val manager: FragmentManager = supportFragmentManager
                val transaction: FragmentTransaction = manager.beginTransaction()
                renameSceneDialog.show(transaction, "dialog")
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

        var dropPosition: Int = -1
        var dy = 0f

        @SuppressLint("ClickableViewAccessibility")
        override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val selectedPosition = viewHolder.bindingAdapterPosition
            val isActive: Boolean =
                adapter.isExpanded(selectedPosition) // retrieve your model from list and check its active state
            return if (!isActive) super.getDragDirs(recyclerView, viewHolder) else 0
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.onItemMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
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
            Log.d("DROP", "dY - $dy")
            if (viewHolder.bindingAdapterPosition >= 0 && viewHolder.bindingAdapterPosition < adapter.itemCount &&
                viewHolder is RVSceneComponentsAdapter.ViewHolderLamp
            ) {
                if (viewHolder.bindingAdapterPosition != 0 && dY < -viewHolder.lampObject.height - 50) {
                    adapter.addLampInGroup(
                        viewHolder.bindingAdapterPosition,
                        viewHolder.bindingAdapterPosition - 1
                    )
                }
                if (viewHolder.bindingAdapterPosition != adapter.itemCount - 1 && dY > viewHolder.lampObject.height + 50) {
                    adapter.addLampInGroup(
                        viewHolder.bindingAdapterPosition,
                        viewHolder.bindingAdapterPosition + 1
                    )
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            Log.d("DROP", "ViewHolder drag position - ${viewHolder.bindingAdapterPosition}")
            Log.d("DROP", "ViewHolder drop position - $dropPosition")
            if (dropPosition >= 0 && dropPosition < adapter.itemCount) {
                adapter.addLampInGroup(viewHolder.bindingAdapterPosition, dropPosition)
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

    }

    private fun fetchSceneComponents() {
        try {
            adapter.setData(scenes[num].sceneComponents)
        } catch (e : NullPointerException) {
            Log.e("DBG_TAG", "null in fun fetchAll")
        }
    }

}
