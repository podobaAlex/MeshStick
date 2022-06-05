package com.example.meshstick_withoutmesh.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.ScenesActivity
import com.example.meshstick_withoutmesh.types.Scene
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R
import io.paperdb.Paper

class RVSceneAdapter(private val activity: ScenesActivity) :
    RecyclerView.Adapter<RVSceneAdapter.ViewHolder>() {

    //Объекты находящиеся в scene_rv.xml
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_sceneName)
        val numOfLamps: TextView = itemView.findViewById(R.id.tv_numOfLampsNum)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_control)
        val switchKey: Switch = itemView.findViewById(R.id.s_turnScene)
    }

    //Добавление сцены
    fun add(scene: Scene) {
        scenes.add(scene)
        notifyDataSetChanged()

        Paper.book().write("scenes", scenes)
    }

    fun add(position: Int, scene: Scene) {
        if (position == scenes.size) {
            scenes.add(scene)
        } else {
            scenes.add(position, scene)
        }
        notifyItemInserted(position)

        Paper.book().write("scenes", scenes)
    }

    fun removeScene(position: Int) {
        scenes.removeAt(position)
        //scenes.remove(scene)
        notifyDataSetChanged()

        Paper.book().write("scenes", scenes)
    }

    //Создание объекта
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scene_rv, parent, false)
        return ViewHolder(view)
    }

    //Обработка объектов scene_rv.xml
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Установка имени сцены
        holder.textView.text = scenes[position].getName()
        //Установка кол-ва ламп
        holder.numOfLamps.text = scenes[position].sceneComponents.size.toString()

        holder.switchKey.isChecked = scenes[position].isActive

        holder.switchKey.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                scenes.forEach { if (it.isActive) it.isActive = false }
                scenes[position].isActive = true
            } else {
                scenes[position].isActive = false
            }
        }

        //Переход в LampsScene
        holder.btSettings.setOnClickListener {
            activity.sceneLauncher.launch(
                Intent(activity, SceneComponentsActivity::class.java).putExtra("num", position)
            )
        }

    }

    override fun getItemCount(): Int {
        return scenes.size
    }

    fun setData() {
        notifyDataSetChanged()
    }

}