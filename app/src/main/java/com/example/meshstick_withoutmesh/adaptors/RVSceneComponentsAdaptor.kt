package com.example.meshstick_withoutmesh.adaptors

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.SettingsActivity
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.SceneComponents
import com.example.myapplication.R
import java.util.*

class RVSceneComponentsAdaptor(
    private var items: MutableList<SceneComponents>,
    private val activity: SceneComponentsActivity
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //Меняем лампы местами
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        notifyItemRangeChanged(fromPosition, kotlin.math.abs(toPosition - fromPosition))
        return true
    }

    //Изменяем данные лампы
    fun changeData(sceneComponent: SceneComponents, position: Int) {
        items[position] = sceneComponent
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

    //Объекты lamp_rv.xml
    class ViewHolderLamp(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.current_color)
    }

    class ViewHolderGroup(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.current_color)
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
        holder.textView.text = items[position].name
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
                items[position].red,
                items[position].green,
                items[position].blue
            )
        )
    }

    private fun onBindViewHolderGroup(holder: ViewHolderGroup, position: Int) {
        //Установка имени лампы
        holder.textView.text = items[position].name
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
                items[position].red,
                items[position].green,
                items[position].blue
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }


}