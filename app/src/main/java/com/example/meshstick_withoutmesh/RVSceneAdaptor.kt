package com.example.meshstick_withoutmesh

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RVSceneAdaptor(private val scenes: MutableList<Scene>, private val activity: SceneActivity): RecyclerView.Adapter<RVSceneAdaptor.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_sceneName)
        val numOfLamps: TextView = itemView.findViewById(R.id.tv_numOfLampsNum)
        val bt_setting = itemView.findViewById<FloatingActionButton>(R.id.bt_control)
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

        holder.bt_setting.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)

            intent.putExtra("num", position)
            activity.startActivityForResult(intent, 1)
        }
    }

    override fun getItemCount(): Int {
        return scenes.size
    }
}