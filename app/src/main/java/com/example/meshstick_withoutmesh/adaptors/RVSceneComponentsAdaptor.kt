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
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.LampSettingsActivity
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.SceneComponents
import com.example.myapplication.R
import java.util.*

class RVSceneComponentsAdaptor(
    private var items: MutableList<SceneComponents>,
    private val activity: SceneComponentsActivity
) :
    RecyclerView.Adapter<RVSceneComponentsAdaptor.ViewHolder>() {

    //Меняем лампы местами
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    //Изменяем данные лампы
    fun changeData(
        name: String,
        position: Int,
        red: Int, green: Int, blue: Int
    ) {

        items[position].name = name
        items[position].red = red
        items[position].green = green
        items[position].blue = blue

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
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.current_color)
    }

    //Создание объекта
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lamp_rv, parent, false)
        return ViewHolder(view)
    }

    //Обработка объектов
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Установка имени лампы
        holder.textView.text = items[position].name
        //Переход в LampSettingsActivity
        holder.btSettings.setOnClickListener {
            val intent = Intent(activity, LampSettingsActivity::class.java)

            intent.putExtra("name", items[position].name)
            intent.putExtra("red", items[position].red.toString())
            intent.putExtra("green", items[position].green.toString())
            intent.putExtra("blue", items[position].blue.toString())
            intent.putExtra("position_settings", position)

            if (items[position] is Group) {
                Log.d("GROUP", "THIS IS ${items[position].name}")
            }

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