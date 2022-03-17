package com.example.meshstick_withoutmesh.adaptors

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.SavedScenesActivity
import com.example.meshstick_withoutmesh.types.Scene
import com.example.myapplication.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RVSavedSceneAdaptor(
    private val scenes: MutableList<Scene>,
    private val savedScenesActivity: SavedScenesActivity
) : RecyclerView.Adapter<RVSavedSceneAdaptor.ViewHolder>() {
    //Объекты saved_scene_rv.xml
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sceneName: TextView = itemView.findViewById(R.id.tv_savedSceneName)
        val numOfLamps: TextView = itemView.findViewById(R.id.tv_savedNumOfLampsNum)
        val btLoad: FloatingActionButton = itemView.findViewById(R.id.bt_load)
    }

    //Создание объекта
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.saved_scene_rv, parent, false)
        return ViewHolder(view)
    }

    //Обработка объектов
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Установка имени сцены
        holder.sceneName.text = scenes[position].getName()
        //Установка кол-ва ламп
        holder.numOfLamps.text = scenes[position].lamps.size.toString()
        //Загрузить сохранённую сцену
        holder.btLoad.setOnClickListener {
            val intent = Intent()

            intent.putExtra("scene_name", scenes[position].getName())
            intent.putExtra("lamps_count", scenes[position].lamps.size)
            for (i in 0 until scenes[position].lamps.size) {
                intent.putExtra("lamp${i}_name", scenes[position].lamps[i].getName())
                intent.putExtra("lamp${i}_red", scenes[position].lamps[i].red)
                intent.putExtra("lamp${i}_green", scenes[position].lamps[i].green)
                intent.putExtra("lamp${i}_blue", scenes[position].lamps[i].blue)
            }

            savedScenesActivity.setResult(2, intent)
        }
    }

    override fun getItemCount(): Int {
        return scenes.size
    }
}