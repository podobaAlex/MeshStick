package com.example.meshstick_withoutmesh.adaptors

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.ScenesActivity
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.Scene
import com.example.myapplication.R

class RVSceneAdaptor(private val scenes: MutableList<Scene>, private val activity: ScenesActivity) :
    RecyclerView.Adapter<RVSceneAdaptor.ViewHolder>() {

    //Количество сохранённых сцен
    private var saveCounter: Int = 0

    //БД с сохранёнными сценами
    private var pref: SharedPreferences = activity.getSharedPreferences("Scenes", AppCompatActivity.MODE_PRIVATE)

    //Объекты находящиеся в scene_rv.xml
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_sceneName)
        val numOfLamps: TextView = itemView.findViewById(R.id.tv_numOfLampsNum)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_control)
        val btSave: AppCompatImageButton = itemView.findViewById(R.id.bt_saveScene)
    }

    //Добавление сцены
    fun add(scene: Scene) {
        this.scenes.add(scene)
        notifyDataSetChanged()
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

        //Переход в LampsScene
        holder.btSettings.setOnClickListener {
            activity.sceneLauncher.launch(
                Intent(activity, SceneComponentsActivity::class.java).putExtra("num", position)
            )
        }

        //Сохранение сцены
        holder.btSave.setOnClickListener { saveScene(position) }

    }

    //Сохранение сцены
    private fun saveScene(position: Int) {
        val editor = pref.edit()
        saveCounter = 0
        saveCounter = pref.getInt("save_count", 0)
        Log.d("SAVE", "$saveCounter")
        editor.putString("scene$saveCounter", scenes[position].getName())
        editor.putInt("scene${saveCounter}_size", scenes[position].sceneComponents.size)
        for (i in 0 until scenes[position].sceneComponents.size) {
            if (scenes[position].sceneComponents[i] is Lamp) {
                editor.putString("scene${saveCounter}_lamp${i}", scenes[position].sceneComponents[i].name)
                editor.putInt("scene${saveCounter}_lamp${i}_red", scenes[position].sceneComponents[i].red)
                editor.putInt("scene${saveCounter}_lamp${i}_green", scenes[position].sceneComponents[i].green)
                editor.putInt("scene${saveCounter}_lamp${i}_blue", scenes[position].sceneComponents[i].blue)
            } else {
                editor.putString("scene${saveCounter}_group${i}", scenes[position].sceneComponents[i].name)
                editor.putInt("scene${saveCounter}_group${i}_red", scenes[position].sceneComponents[i].red)
                editor.putInt("scene${saveCounter}_group${i}_green", scenes[position].sceneComponents[i].green)
                editor.putInt("scene${saveCounter}_group${i}_blue", scenes[position].sceneComponents[i].blue)
                val group: Group = scenes[position].sceneComponents[i] as Group
                for (j in 0 until group.lamps.size) {
                    editor.putString("scene${saveCounter}_group${i}_lamp${j}", group.name)
                }
            }
        }
        editor.putInt("save_count", ++saveCounter)
        editor.apply()
    }

    override fun getItemCount(): Int {
        return scenes.size
    }
}