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
import com.example.meshstick_withoutmesh.LampSettingsActivity
import com.example.meshstick_withoutmesh.LampsActivity
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.myapplication.R
import java.util.*

class RVLampAdaptor(private var items: MutableList<Lamp>, private val activity: LampsActivity) :
    RecyclerView.Adapter<RVLampAdaptor.ViewHolder>() {

    //Меняем лампы местами
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) for (i in fromPosition until toPosition) Collections.swap(items, i, i + 1)
        else for (i in fromPosition downTo toPosition + 1) Collections.swap(items, i, i - 1)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    //Изменяем данные лампы
    fun changeData(
        name: String,
        position: Int,
        red: Int, green: Int, blue: Int
    ) {

        items[position].setName(name)
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
        holder.textView.text = items[position].getName()
        //Переход в LampSettingsActivity
        holder.btSettings.setOnClickListener {
            val intent = Intent(activity, LampSettingsActivity::class.java)

            intent.putExtra("name", items[position].getName())
            intent.putExtra("red", items[position].red.toString())
            intent.putExtra("green", items[position].green.toString())
            intent.putExtra("blue", items[position].blue.toString())
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