package com.example.meshstick_withoutmesh.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.connectedLamps
import com.example.myapplication.R

class RVConnectedLampsAdapter : RecyclerView.Adapter<RVConnectedLampsAdapter.ViewHolderLamp>() {

    //Добавление новой лампы
    fun addLamp(lamp: Lamp) {
        connectedLamps.add(lamp)
        notifyDataSetChanged()
    }

    //Объекты lamp_rv.xml
    class ViewHolderLamp(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.current_color)
    }

    //Создание объекта
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderLamp {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lamp_rv, parent, false)
        return ViewHolderLamp(view)
    }

    //Обработка объектов
    override fun onBindViewHolder(holder: ViewHolderLamp, position: Int) {
        val lamp = connectedLamps[position]
        holder.textView.text = lamp.name
        //Обновление цвета
        holder.currentColor.setBackgroundColor(
            Color.rgb(
                lamp.red,
                lamp.green,
                lamp.blue
            )
        )
    }

    override fun getItemCount(): Int {
        return connectedLamps.size
    }

}