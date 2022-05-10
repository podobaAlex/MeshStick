package com.example.meshstick_withoutmesh.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.MainActivity
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.myapplication.R

class RVConnectedMeshesAdapter(private val activity: MainActivity) :
    RecyclerView.Adapter<RVConnectedMeshesAdapter.ViewHolderMesh>() {

    //Объекты lamp_rv.xml
    class ViewHolderMesh(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_groupName)
        val btSettings: AppCompatImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.ll_color)
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
        holder.currentColor.visibility = View.GONE
        holder.addLamp.visibility = View.GONE
        holder.rvLamps.layoutManager = LinearLayoutManager(activity)
        holder.adapter = RVLampsOfMeshAdapter(position)
        holder.rvLamps.adapter = holder.adapter

//        holder.textView.setOnClickListener {
//            val meshPasswordDialogFragment = MeshPasswordDialogFragment(position)
//            val manager: FragmentManager = activity.supportFragmentManager
//            val transaction: FragmentTransaction = manager.beginTransaction()
//            meshPasswordDialogFragment.show(transaction, "dialog")
//        }

    }

    override fun getItemCount(): Int {
        return connectedMeshes.size
    }

}