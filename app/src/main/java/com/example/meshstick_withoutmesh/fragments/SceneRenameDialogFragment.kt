package com.example.meshstick_withoutmesh.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.types.scenes
import io.paperdb.Paper

class SceneRenameDialogFragment(private val num: Int) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setTitle("Set name for scene")

            val input = EditText(it)
            input.inputType = InputType.TYPE_CLASS_TEXT
            input.setText(scenes[num].getName())
            builder.setView(input)

            builder.setPositiveButton("Save") { dialog, id ->
                run {
                    scenes[num].setName(input.text.toString())
                    (activity as SceneComponentsActivity).supportActionBar!!.title = scenes[num].getName()

                    Paper.book().write("scenes", scenes)
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}