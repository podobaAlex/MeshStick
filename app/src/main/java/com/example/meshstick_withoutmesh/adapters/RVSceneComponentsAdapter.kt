package com.example.meshstick_withoutmesh.adapters

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
import com.example.meshstick_withoutmesh.types.*
import com.example.myapplication.R
import io.paperdb.Paper
import java.util.*

class RVSceneComponentsAdapter(
    private val num: Int,
    private val activity: SceneComponentsActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //Меняем лампы местами
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        var start = fromPosition
        var end = toPosition
        Collections.swap(scenes[num].sceneComponents, start, end)
        notifyItemMoved(start, end)
        if (toPosition < fromPosition) start = toPosition.also { end = fromPosition }
        notifyItemRangeChanged(start, kotlin.math.abs(start - end) + 1)

        Paper.book().write("scenes", scenes)
    }

    //Изменяем данные лампы
    fun changeData(sceneComponent: SceneComponents, position: Int) {
        if (sceneComponent is Group) {
            sceneComponent.expanded = (scenes[num].sceneComponents[position] as Group).expanded
        }
        scenes[num].sceneComponents[position] = sceneComponent
        notifyDataSetChanged()

        Paper.book().write("scenes", scenes)
    }

    fun addLampInGroup(lampPosition: Int, groupPosition: Int) {
        if (!(scenes[num].sceneComponents[groupPosition] is Group
                    && scenes[num].sceneComponents[lampPosition] is Lamp)
        ) return

        if (!(scenes[num].sceneComponents[groupPosition] as Group).expanded) return

        val lamp = scenes[num].sceneComponents[lampPosition] as Lamp
        (scenes[num].sceneComponents[groupPosition] as Group).lamps.add(GroupedLamp(lamp))
        scenes[num].sceneComponents.removeAt(lampPosition)
        notifyDataSetChanged()

        Paper.book().write("scenes", scenes)
    }

    //Добавление новой лампы
    fun addLamp(lamp: Lamp) {
        scenes[num].sceneComponents.add(lamp)
        notifyDataSetChanged()

        Paper.book().write("scenes", scenes)
    }

    fun addLamp(index: Int, lamp: Lamp) {
        scenes[num].sceneComponents.add(index, lamp)
        notifyDataSetChanged()
    }

    //Добавление новой группы
    fun addGroup(group: Group) {
        scenes[num].sceneComponents.add(group)
        notifyDataSetChanged()

        Paper.book().write("scenes", scenes)
    }

    fun updateLampInGroup(lamp: GroupedLamp, groupPosition: Int, lampPosition: Int) {
        (scenes[num].sceneComponents[groupPosition] as Group).lamps[lampPosition] = lamp
        notifyItemChanged(groupPosition)

        Paper.book().write("scenes", scenes)
    }

    fun removeComponent(position: Int) {
        scenes[num].sceneComponents.removeAt(position)
        notifyDataSetChanged()

        Paper.book().write("scenes", scenes)
    }

    //Объекты lamp_rv.xml
    class ViewHolderLamp(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.current_color)
        val lampObject: LinearLayout = itemView.findViewById(R.id.lamp_object)
    }

    //Объекты group_rv.xml
    class ViewHolderGroup(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_groupName)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.ll_color)
        val rvLamps: RecyclerView = itemView.findViewById(R.id.rv_lampsOfGroup)
        val addLampPosition: TextView = itemView.findViewById(R.id.tv_addLampPosition)
        lateinit var adapter: RVLampsOfGroupAdapter

        private val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    or ItemTouchHelper.END or ItemTouchHelper.START, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.onItemMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
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
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                Log.d("dY_LAMP_IN_GROUP", "$dY")
                if (viewHolder.bindingAdapterPosition == 0 && dY < -(viewHolder as RVLampsOfGroupAdapter.LampHolder).lampObject.height - 50) {
                    adapter.outOfGroup(bindingAdapterPosition, viewHolder.bindingAdapterPosition)
                }
                if (viewHolder.bindingAdapterPosition == adapter.itemCount - 1 && dY > (viewHolder as RVLampsOfGroupAdapter.LampHolder).lampObject.height + 50) {
                    adapter.outOfGroup(bindingAdapterPosition + 1, viewHolder.bindingAdapterPosition)
                }
            }

        }

        private val itemTouchHelper = ItemTouchHelper(simpleCallback)

        init {
            itemTouchHelper.attachToRecyclerView(rvLamps)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (scenes[num].sceneComponents[position]) {
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
        val lamp = scenes[num].sceneComponents[position] as Lamp
        holder.textView.text = lamp.name
        //Переход в LampSettingsActivity
        holder.btSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)

            intent.putExtra("component", scenes[num].sceneComponents[position] as Lamp)
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
        val group = scenes[num].sceneComponents[position] as Group
        holder.textView.text = group.name

        holder.rvLamps.layoutManager = LinearLayoutManager(activity)
        holder.adapter =
            RVLampsOfGroupAdapter(group.lamps, activity, position, Color.rgb(group.red, group.green, group.blue))
        holder.rvLamps.adapter = holder.adapter

        holder.currentColor.setOnClickListener {
            group.expanded = !group.expanded
            notifyItemChanged(position)
            Log.d("GROUP", "EXPANDED-${group.expanded}")
        }

        val isExpandable = group.expanded
        holder.rvLamps.visibility = if (isExpandable) View.VISIBLE else View.GONE
        holder.addLampPosition.visibility = if (isExpandable) View.VISIBLE else View.GONE

        //Переход в LampSettingsActivity
        holder.btSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)

            intent.putExtra("component", scenes[num].sceneComponents[position] as Group)
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
        return scenes[num].sceneComponents.size
    }

    // функция для восстановления информации при запуске приложения
    fun setData(items: MutableList<SceneComponents>) {
        scenes[num].sceneComponents = items
        notifyDataSetChanged()
    }

    fun isExpanded(position: Int): Boolean {
        val item = scenes[num].sceneComponents[position]
        if (item is Group) return item.expanded
        return false
    }

}