package com.example.meshstick_withoutmesh.adaptors

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.SettingsActivity
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.GroupedLamp
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.SceneComponents
import com.example.myapplication.R
import java.util.*

class RVSceneComponentsAdaptor(
    var items: MutableList<SceneComponents>,
    private val activity: SceneComponentsActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //Меняем лампы местами
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        var start = fromPosition
        var end = toPosition
        Collections.swap(items, start, end)
        notifyItemMoved(start, end)
        if (toPosition < fromPosition) start = toPosition.also { end = fromPosition }
        notifyItemRangeChanged(start, kotlin.math.abs(start - end) + 1)
    }

    //Изменяем данные лампы
    fun changeData(sceneComponent: SceneComponents, position: Int) {
        items[position] = sceneComponent
        notifyDataSetChanged()
    }

    fun addLampInGroup(lampPosition: Int, groupPosition: Int) {
        if (!(items[groupPosition] is Group && items[lampPosition] is Lamp)) return
        val lamp = items[lampPosition] as Lamp
        (items[groupPosition] as Group).lamps.add(GroupedLamp(lamp))
        items.removeAt(lampPosition)
        notifyDataSetChanged()
    }

    //Добавление новой лампы
    fun addLamp(lamp: Lamp) {
        items.add(lamp)
        notifyDataSetChanged()
    }

    fun addLamp(index: Int, lamp: Lamp) {
        items.add(index, lamp)
        notifyDataSetChanged()
    }

    //Добавление новой группы
    fun addGroup(group: Group) {
        items.add(group)
        notifyDataSetChanged()
    }

    fun updateLampInGroup(lamp: GroupedLamp, groupPosition: Int, lampPosition: Int) {
        (items[groupPosition] as Group).lamps[lampPosition] = lamp
        notifyItemChanged(groupPosition)
    }

    //Объекты lamp_rv.xml
    class ViewHolderLamp(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.current_color)
    }

    //Объекты group_rv.xml
    class ViewHolderGroup(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_groupName)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.ll_color)
        val rvLamps: RecyclerView = itemView.findViewById(R.id.rv_lampsOfGroup)
        lateinit var adaptor: RVLampsOfGroup
        private var pos = this.adapterPosition

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

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
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
                        dY > 0.8f -> pos
                        dY < -0.8f -> pos - 1
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
                if (viewHolder.adapterPosition == 0 || viewHolder.adapterPosition == adaptor.itemCount - 1) {
                    if (dy > 0.8f) {
                        adaptor.outOfGroup(adapterPosition + 1, viewHolder.adapterPosition)
                    }
                    if (dy < -0.8f) {
                        adaptor.outOfGroup(adapterPosition, viewHolder.adapterPosition)
                    }
                }
            }

        }

        private val itemTouchHelper = ItemTouchHelper(simpleCallback)

        init {
            itemTouchHelper.attachToRecyclerView(rvLamps)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Lamp -> 0
            is Group -> 1
            else -> throw Exception("Wrong Type")
        }
    }

    //Создание объекта
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.lamp_rv, parent, false)
                ViewHolderLamp(view)
            }
            1 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.group_rv, parent, false)
                ViewHolderGroup(view)
            }
            else -> throw Exception("Wrong Type")
        }
    }

    //Обработка объектов
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderLamp -> onBindViewHolderLamp(holder, position)
            is ViewHolderGroup -> onBindViewHolderGroup(holder, position)
        }
    }

    private fun onBindViewHolderLamp(holder: ViewHolderLamp, position: Int) {
        //Установка имени лампы
        val lamp = items[position] as Lamp
        holder.textView.text = lamp.name
        //Переход в LampSettingsActivity
        holder.btSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)

            intent.putExtra("component", items[position] as Lamp)
            intent.putExtra("position_settings", position)

            activity.lampsLauncher.launch(intent)
        }
        //Обновление цвета
        holder.currentColor.setBackgroundColor(
            Color.rgb(
                lamp.red,
                lamp.green,
                lamp.blue
            )
        )
    }

    private fun onBindViewHolderGroup(holder: ViewHolderGroup, position: Int) {
        //Установка имени лампы
        val group = items[position] as Group
        holder.textView.text = group.name

        holder.rvLamps.layoutManager = LinearLayoutManager(activity)
        holder.adaptor = RVLampsOfGroup(group.lamps, activity, position, Color.rgb(group.red, group.green, group.blue))
        holder.rvLamps.adapter = holder.adaptor

        holder.currentColor.setOnClickListener {
            group.expanded = !group.expanded
            notifyItemChanged(position)
            Log.d("GROUP", "EXPANDED-${group.expanded}")
        }

        val isExpandable = group.expanded
        holder.rvLamps.visibility = if (isExpandable) View.VISIBLE else View.GONE

        //Переход в LampSettingsActivity
        holder.btSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)

            intent.putExtra("component", items[position] as Group)
            intent.putExtra("position_settings", position)

            activity.lampsLauncher.launch(intent)
        }
        //Обновление цвета
        holder.currentColor.setBackgroundColor(
            Color.rgb(
                group.red,
                group.green,
                group.blue
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun isExpanded(position: Int): Boolean {
        val item = items[position]
        if (item is Group) return item.expanded
        return false
    }

}