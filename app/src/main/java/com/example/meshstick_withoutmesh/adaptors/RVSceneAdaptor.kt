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
import com.example.meshstick_withoutmesh.LampsActivity
import com.example.meshstick_withoutmesh.ScenesActivity
import com.example.meshstick_withoutmesh.types.Scene
import com.example.myapplication.R

class RVSceneAdaptor(private val scenes: MutableList<Scene>, private val activity: ScenesActivity) :
    RecyclerView.Adapter<RVSceneAdaptor.ViewHolder>() {

    private var saveCounter: Int = 0
    private var pref: SharedPreferences = activity.getSharedPreferences("Scenes", AppCompatActivity.MODE_PRIVATE)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_sceneName)
        val numOfLamps: TextView = itemView.findViewById(R.id.tv_numOfLampsNum)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_control)
        val btSave: AppCompatImageButton = itemView.findViewById(R.id.bt_saveScene)
    }

    fun add(scene: Scene) {
        this.scenes.add(scene)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scene_rv, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = scenes[position].getName()
        holder.numOfLamps.text = scenes[position].lamps.size.toString()

        holder.btSettings.setOnClickListener {
            activity.sceneLauncher.launch(Intent(activity, LampsActivity::class.java).putExtra("num", position))
        }

        holder.btSave.setOnClickListener { saveScene(position) }

    }

    private fun saveScene(position: Int) {
        val editor = pref.edit()
        saveCounter = 0
        saveCounter = pref.getInt("save_count", 0)
        Log.d("SAVE", "$saveCounter")
        editor.putString("scene$saveCounter", scenes[position].getName())
        editor.putInt("scene${saveCounter}_size", scenes[position].lamps.size)
        for (i in 0 until scenes[position].lamps.size) {
            editor.putString("scene${saveCounter}_lamp$i", scenes[position].lamps[i].getName())
            editor.putInt("scene${saveCounter}_lamp${i}_red", scenes[position].lamps[i].red)
            editor.putInt("scene${saveCounter}_lamp${i}_green", scenes[position].lamps[i].green)
            editor.putInt("scene${saveCounter}_lamp${i}_blue", scenes[position].lamps[i].blue)
        }
        editor.putInt("save_count", ++saveCounter)
        editor.apply()
    }

    override fun getItemCount(): Int {
        return scenes.size
    }
}