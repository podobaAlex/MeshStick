package com.example.meshstick_withoutmesh.adaptors

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
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
    private var items: MutableList<SceneComponents>,
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


}