package com.example.meshstick_withoutmesh

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adapters.RVSceneComponentsAdapter
import com.example.meshstick_withoutmesh.fragments.MeshDialogFragment
import com.example.meshstick_withoutmesh.fragments.SceneRenameDialogFragment
import com.example.meshstick_withoutmesh.types.*
import com.example.myapplication.R
import com.google.android.material.snackbar.Snackbar

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
                        val componentPosition = result.data!!.getIntExtra("position_comeback", 0)
                        adapter.changeData(
                            result.data!!.getParcelableExtra("component")!!,
                            componentPosition
                        )
                        if (scenes[num].isActive) {
                            if (scenes[num].sceneComponents[componentPosition] is Colored) {
                                (scenes[num].sceneComponents[componentPosition] as Colored).sendToMesh()
                            }
                        }
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
        val btAdd: ImageView = findViewById(R.id.bt_add)

        val recyclerView: RecyclerView = findViewById(R.id.rl_components)
        recyclerView.layoutManager = LinearLayoutManager(this)
        num = this.intent.getIntExtra("num", 0)

        adapter = RVSceneComponentsAdapter(num, this)
        recyclerView.adapter = adapter

        //восстанавливаем информацию из хранилища
        fetchSceneComponents()

        var btnExpanded = false

        btAdd.setOnClickListener {
            if (!btnExpanded) {
                animateButton(btAdd)
                activateButtons(btAddGroup, btAddLamp, true)
                // анимация для кнопок addGroup, addLamp
                scaleButton(btAddGroup, 0.6f, 1.0f)
                scaleButton(btAddLamp, 0.6f, 1.0f)

                btnExpanded = true

            } else {
                val reverse = true
                animateButton(btAdd, reverse)

                scaleButton(btAddGroup, 1.0f, 0.6f)
                scaleButton(btAddLamp, 1.0f, 0.6f)

                activateButtons(btAddGroup, btAddLamp, false)
                btnExpanded = false
            }
        }
        // скрываем кнопки addLamp и addGroup при скроллинге rv
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (btnExpanded) {
                    // обратная анимация для кнопки btAdd
                    val reverse = true
                    animateButton(btAdd, reverse)
                    // обратная анимация и отключение кнопок btAddGroup, btAddLamp
                    scaleButton(btAddGroup, 1.0f, 0.6f)
                    scaleButton(btAddLamp, 1.0f, 0.6f)

                    activateButtons(btAddGroup, btAddLamp, false)
                    btnExpanded = false
                }
            }
        })

        btAddLamp.setOnClickListener {
            val addLampDialog = MeshDialogFragment(num)
            val manager: FragmentManager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            addLampDialog.show(transaction, "dialog")

            val reverse = true
            animateButton(btAdd, reverse)

            scaleButton(btAddGroup, 1.0f, 0.6f)
            scaleButton(btAddLamp, 1.0f, 0.6f)

            activateButtons(btAddGroup, btAddLamp, false)
            btnExpanded = false
        }
        btAddGroup.setOnClickListener {
            adapter.addGroup(Group("group"))

            val reverse = true
            animateButton(btAdd, reverse)

            scaleButton(btAddGroup, 1.0f, 0.6f)
            scaleButton(btAddLamp, 1.0f, 0.6f)

            activateButtons(btAddGroup, btAddLamp, false)
            btnExpanded = false

        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // свайп влево - удаление
        val swipeGesture = object : SwipeGesture(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    val position = viewHolder.bindingAdapterPosition
                    val deletedItem = scenes[num].sceneComponents[position]
                    adapter.removeComponent(position)
                    Snackbar.make(recyclerView, "Элемент удален", Snackbar.LENGTH_LONG)
                        .setAction("Отменить") {
                            when (deletedItem) {
                                is Lamp -> adapter.addLamp(position, deletedItem)
                                is Group -> adapter.addGroup(position, deletedItem)
                            }
                        }
                        .show()
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
                val renameSceneDialog = SceneRenameDialogFragment(num)
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
                Log.d("DROP", "${viewHolder.lampObject.height}")
                if (viewHolder.bindingAdapterPosition != 0 && dY < -viewHolder.lampObject.height + 25) {
                    Log.d("LAMP ADDED IN GROUP", "UP")
                    adapter.addLampInGroup(
                        viewHolder.bindingAdapterPosition,
                        viewHolder.bindingAdapterPosition - 1
                    )
                }
                if (viewHolder.bindingAdapterPosition != adapter.itemCount - 1 && dY > viewHolder.lampObject.height - 25) {
                    Log.d("LAMP ADDED IN GROUP", "DOWN")
                    adapter.addLampInGroup(
                        viewHolder.bindingAdapterPosition,
                        viewHolder.bindingAdapterPosition + 1
                    )
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

    }

    private fun fetchSceneComponents() {
        try {
            adapter.setData(scenes[num].sceneComponents)
        } catch (e: NullPointerException) {
            Log.e("DBG_TAG", "null in fun fetchAll")
        }
    }


    // функция для активации кнопок addGroup, addLamp
    private fun activateButtons(btAddGroup: Button, btAddLamp: Button, value: Boolean) {
        btAddLamp.isVisible = value
        btAddLamp.isEnabled = value
        btAddGroup.isVisible = value
        btAddGroup.isEnabled = value
    }

    private fun scaleButton(button: Button, fromValue: Float, toValue: Float) {
        val set = AnimatorSet()
        val scaleX = ObjectAnimator.ofFloat(button, View.SCALE_X, fromValue, toValue).setDuration(200)
        val scaleY = ObjectAnimator.ofFloat(button, View.SCALE_Y, fromValue, toValue).setDuration(200)
        set.playTogether(scaleX, scaleY)
        set.start()
    }

    private fun animateButton(btAdd: ImageView, reverseDirection: Boolean = false) {
        if (reverseDirection) {
            btAdd.setImageResource(R.drawable.avd_anim_reverse)
        } else {
            btAdd.setImageResource(R.drawable.avd_anim)
        }
        val animation = btAdd.drawable as AnimatedVectorDrawable
        animation.start()
    }
}
