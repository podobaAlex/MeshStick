package com.example.meshstick_withoutmesh.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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

    //Объекты mesh_rv.xml
    class ViewHolderMesh(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_meshName)
        val btSettings: ImageButton = itemView.findViewById(R.id.bt_settings)
        val currentColor: LinearLayout = itemView.findViewById(R.id.ll_color)
        val rvLamps: RecyclerView = itemView.findViewById(R.id.rv_lampsOfMesh)
        lateinit var adapter: RVLampsOfMeshAdapter
    }

    //Создание объекта
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMesh {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mesh_rv, parent, false)
        return ViewHolderMesh(view)
    }

    //Обработка объектов
    override fun onBindViewHolder(holder: ViewHolderMesh, position: Int) {
        holder.textView.text = connectedMeshes[position].name
        holder.btSettings.visibility = View.GONE
        if (connectedMeshes[position].isConnected) {
            holder.currentColor.setBackgroundColor(Color.rgb(0, 255, 0))
        } else {
            holder.currentColor.setBackgroundColor(Color.rgb(255, 0, 0))
        }
        holder.rvLamps.layoutManager = LinearLayoutManager(activity)
        holder.adapter = RVLampsOfMeshAdapter(activity, position)
        holder.rvLamps.adapter = holder.adapter

        holder.textView.setOnClickListener {
            Log.d("MainActivity", connectedMeshes[position].MAC)
            val meshPasswordDialogFragment = MeshPasswordDialogFragment(position, activity)
            val manager: FragmentManager = activity.supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            meshPasswordDialogFragment.show(transaction, "dialog")
        }

    }

    override fun getItemCount(): Int {
        return connectedMeshes.size
    }

}