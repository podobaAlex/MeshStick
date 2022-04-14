package com.example.meshstick_withoutmesh.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.SettingsActivity
import com.example.meshstick_withoutmesh.types.GroupedLamp
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.myapplication.R
import java.util.*

class RVLampsOfGroupAdapter(
    private val lamps: MutableList<GroupedLamp>,
    private val activity: SceneComponentsActivity,
    private val groupPosition: Int,
    private val color: Int
) : RecyclerView.Adapter<RVLampsOfGroupAdapter.LampHolder>() {

    class LampHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.current_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LampHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lamp_rv, parent, false)
        Log.d("GROUP", "LAMP_HOLDED")
        return LampHolder(view)
    }

    @SuppressLint("ShowToast")
    override fun onBindViewHolder(holder: LampHolder, position: Int) {
        //Установка имени лампы
        holder.textView.text = lamps[position].name
        //Переход в LampSettingsActivity
        holder.btSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)

            intent.putExtra("component", lamps[position])
            intent.putExtra("position_settings", position)
            intent.putExtra("group", 1)
            intent.putExtra("group_position", groupPosition)

            activity.lampsLauncher.launch(intent)
        }
        holder.textView.setOnClickListener {
            Toast.makeText(activity, "lamp", Toast.LENGTH_LONG)
            Log.d("GROUP", "LAMP")
        }
        //Обновление цвета
        holder.currentColor.setBackgroundColor(color)
    }

    fun outOfGroup(groupPosition: Int, lampPosition: Int) {
        activity.adapter.addLamp(groupPosition, Lamp(lamps[lampPosition].name))
        lamps.removeAt(lampPosition)
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        var start = fromPosition
        var end = toPosition
        Collections.swap(lamps, start, end)
        notifyItemMoved(start, end)
        if (toPosition < fromPosition) start = toPosition.also { end = fromPosition }
        notifyItemRangeChanged(start, kotlin.math.abs(start - end) + 1)
    }

    override fun getItemCount(): Int {
        return lamps.size
    }
}