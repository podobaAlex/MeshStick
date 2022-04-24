package com.example.meshstick_withoutmesh.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.meshstick_withoutmesh.types.connectedMeshes

class MeshDialogFragment(private val num: Int) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val meshes: Array<String> = connectedMeshes.map { it -> it.name }.toTypedArray()

        return when (meshes.size) {
            0 -> activity?.let {
                val builder = AlertDialog.Builder(it)

                builder.setTitle("Нет доступных сетей").setNeutralButton("OK") { dialog, id -> dialog.cancel() }

                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
            else -> activity?.let {
                val builder = AlertDialog.Builder(it)

                builder.setTitle("Выберите сеть").setItems(meshes) { dialog, which ->
                    val addLampDialog = MeshLampsDialogFragment(which, num)
                    val manager: FragmentManager = activity!!.supportFragmentManager
                    val transaction: FragmentTransaction = manager.beginTransaction()
                    addLampDialog.show(transaction, "dialog")
                }

                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }
}