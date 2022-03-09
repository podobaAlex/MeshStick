package com.example.meshstick_withoutmesh

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class RVLampAdaptor(private var items: MutableList<Lamp>, private val activity: MainActivity) :
    RecyclerView.Adapter<RVLampAdaptor.ViewHolder>() {

    public fun onItemMove(fromPosition: Int, toPosition: Int) : Boolean {
        if(fromPosition < toPosition) for (i in fromPosition until toPosition) Collections.swap(items, i, i+1)
        else for (i in fromPosition downTo toPosition + 1) Collections.swap(items, i, i-1)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    public fun newData(lamps : MutableList<Lamp>) {
        items = lamps
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val bt_settings = itemView.findViewById<FloatingActionButton>(R.id.bt_settings)
        val current_color = itemView.findViewById<LinearLayout>(R.id.current_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lamp_rv, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = items[position].getName()
        holder.bt_settings.setOnClickListener {
            val intent = Intent(activity, LampSettings::class.java)

            intent.putExtra("name", items[position].getName())
            intent.putExtra("red", items[position].red.toString())
            intent.putExtra("green", items[position].green.toString())
            intent.putExtra("blue", items[position].blue.toString())
            intent.putExtra("position", position)

            activity.startActivityForResult(intent, 1)
        }
        holder.current_color.setBackgroundColor(Color.rgb(items[position].red, items[position].green, items[position].blue))
    }

    override fun getItemCount(): Int {
        return items.size
    }


}