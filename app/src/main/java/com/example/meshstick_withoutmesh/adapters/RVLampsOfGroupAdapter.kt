package com.example.meshstick_withoutmesh.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.SettingsActivity
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.scenes
import com.example.myapplication.R
import io.paperdb.Paper
import java.util.*

class RVLampsOfGroupAdapter(
    private val activity: SceneComponentsActivity,
    private val scenePosition: Int,
    private var groupPosition: Int,
    private val color: Int
) : RecyclerView.Adapter<RVLampsOfGroupAdapter.LampHolder>() {

    class LampHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_lampName)
        val btSettings: ImageButton = itemView.findViewById(R.id.bt_settings)
        val lampObject: LinearLayout = itemView.findViewById(R.id.lamp_object)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LampHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lamp_rv, parent, false)
        Log.d("GROUP", "LAMP_HOLDED")
        return LampHolder(view)
    }

    @SuppressLint("ShowToast")
    override fun onBindViewHolder(holder: LampHolder, position: Int) {
        //Установка имени лампы
        holder.textView.text = (scenes[scenePosition].sceneComponents[groupPosition] as Group).lamps[position].name
        //Переход в SettingsActivity
        holder.btSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)

            intent.putExtra(
                "component",
                (scenes[scenePosition].sceneComponents[groupPosition] as Group).lamps[position]
            )
            intent.putExtra("component_position", position)
            intent.putExtra("group", 1)
            intent.putExtra("group_position", groupPosition)
            intent.putExtra("scene_position", scenePosition)

            activity.lampsLauncher.launch(intent)
        }
        holder.textView.setOnClickListener {
            Toast.makeText(activity, "lamp", Toast.LENGTH_LONG)
            Log.d("GROUP", "LAMP")
        }

        //holder.btSettings.setBackgroundColor(color)
        holder.btSettings.setImageDrawable(getGradientDrawable(color))

//        val drawableBt = ContextCompat.getDrawable(activity, R.drawable.ic_group_shape)
//        DrawableCompat.setTint(drawableBt!!, color)
    }

    fun outOfGroup(newPosition: Int, lampPosition: Int) {
        val newLamp = Lamp((scenes[scenePosition].sceneComponents[this.groupPosition] as Group).lamps[lampPosition])
        (scenes[scenePosition].sceneComponents[this.groupPosition] as Group).lamps.removeAt(lampPosition)
        // переносим лампу в другой rv
        activity.adapter.addLamp(
            newPosition,
            newLamp
        )
        if (newPosition == this.groupPosition) groupPosition++

        Paper.book().write("scenes", scenes)
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        var start = fromPosition
        var end = toPosition
        Collections.swap((scenes[scenePosition].sceneComponents[groupPosition] as Group).lamps, start, end)
        notifyItemMoved(start, end)
        if (toPosition < fromPosition) start = toPosition.also { end = fromPosition }
        notifyItemRangeChanged(start, kotlin.math.abs(start - end) + 1)
    }

    override fun getItemCount(): Int {
        return (scenes[scenePosition].sceneComponents[groupPosition] as Group).lamps.size
    }
    private fun getGradientDrawable(
        colorRight: Int,
        colorLeft: Int = ContextCompat.getColor(activity, R.color.mainBlue)
    ): GradientDrawable {
        return GradientDrawable().apply {
            colors = intArrayOf(colorLeft, colorRight)
            gradientType = GradientDrawable.LINEAR_GRADIENT
            shape = GradientDrawable.RECTANGLE
            orientation = GradientDrawable.Orientation.LEFT_RIGHT

        }
    }
}