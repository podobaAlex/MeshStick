package com.example.meshstick_withoutmesh.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.meshstick_withoutmesh.MainActivity
import com.example.meshstick_withoutmesh.types.connectedMeshes

class MeshPasswordDialogFragment(private val num: Int, private val activity: MainActivity) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it)

            builder.setTitle("Password ${connectedMeshes[num].name}")

            val input = EditText(it)
            builder.setView(input)

            builder.setPositiveButton("Save") { dialog, id ->
                run {
                    activity.handleConnection(num, input.text.toString())
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}