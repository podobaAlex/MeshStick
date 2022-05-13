package com.example.meshstick_withoutmesh.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.MainActivity
import com.example.meshstick_withoutmesh.fragments.MeshPasswordDialogFragment
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.myapplication.R

class RVConnectedMeshesAdapter(private val activity: MainActivity) :
    RecyclerView.Adapter<RVConnectedMeshesAdapter.ViewHolderMesh>() {

    //Объекты lamp_rv.xml
    class ViewHolderMesh(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_groupName)
        val btSettings: ImageButton = itemView.findViewById(R.id.bt_settings_group)
        val btExpandMoreLess: ImageButton = itemView.findViewById(R.id.bt_expand)
        val addLamp: TextView = itemView.findViewById(R.id.tv_addLampPosition)
        val rvLamps: RecyclerView = itemView.findViewById(R.id.rv_lampsOfGroup)
        lateinit var adapter: RVLampsOfMeshAdapter
    }

    //Создание объекта
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMesh {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_rv, parent, false)
        return ViewHolderMesh(view)
    }

    //Обработка объектов
    override fun onBindViewHolder(holder: ViewHolderMesh, position: Int) {
        holder.textView.text = connectedMeshes[position].name
        holder.btSettings.visibility = View.GONE
        holder.btExpandMoreLess.visibility = View.GONE
        holder.addLamp.visibility = View.GONE
        holder.rvLamps.layoutManager = LinearLayoutManager(activity.applicationContext)
        holder.adapter = RVLampsOfMeshAdapter(activity.applicationContext, position)
        holder.rvLamps.adapter = holder.adapter

        holder.textView.setOnClickListener {
            val meshPasswordDialogFragment = MeshPasswordDialogFragment(position)
            val manager: FragmentManager = activity.supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            meshPasswordDialogFragment.show(transaction, "dialog")
        }

    }

    override fun getItemCount(): Int {
        return connectedMeshes.size
    }

}