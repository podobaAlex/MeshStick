package com.example.meshstick_withoutmesh.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.myapplication.R

class RVLampsOfMeshAdapter(val context: Context, private val meshPosition: Int) :
    RecyclerView.Adapter<RVLampsOfMeshAdapter.ViewHolderLamp>() {

    class ViewHolderLamp(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lampName: TextView = itemView.findViewById(R.id.tv_lampName)
        val btSettings: ImageButton = itemView.findViewById(R.id.bt_settings)
        //val currentColor: LinearLayout = itemView.findViewById(R.id.current_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderLamp {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lamp_rv, parent, false)
        return ViewHolderLamp(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolderLamp, position: Int) {
        viewHolder.lampName.text = connectedMeshes[meshPosition].lamps[position].name
        //вместо currentColor = invisible
        // TODO
        viewHolder.btSettings.setBackgroundColor(ContextCompat.getColor(context, R.color.mainDarkBlue))

    }

    override fun getItemCount(): Int {
        return connectedMeshes[meshPosition].lamps.size
    }

}