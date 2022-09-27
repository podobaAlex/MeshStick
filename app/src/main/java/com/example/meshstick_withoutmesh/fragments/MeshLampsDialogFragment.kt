package com.example.meshstick_withoutmesh.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.meshstick_withoutmesh.SceneComponentsActivity
import com.example.meshstick_withoutmesh.types.Group
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.meshstick_withoutmesh.types.scenes

class MeshLampsDialogFragment(private val meshName: String, private val num: Int) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentLamps = mutableListOf<Lamp>()
        for (i in scenes[num].sceneComponents) {
            if (i is Lamp) currentLamps.add(i)
            else if (i is Group) i.lamps.forEach { currentLamps.add(Lamp(it)) }
        }

        val remainingLamps = connectedMeshes.find { it.name == meshName }!!.lamps.map { it.id }
            .filter { it -> !currentLamps.map { it.id }.contains(it) }.toTypedArray()

        val checkedLamps = BooleanArray(remainingLamps.size)
        return when (remainingLamps.size) {
            //Диалоговое окно, когда ламп нет
            0 -> activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setTitle("Ламп нет :(").setNeutralButton("OK") { dialog, id -> dialog.cancel() }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
            //Диалоговое окно, когда лампы есть
            else -> activity?.let { it ->
                val builder = AlertDialog.Builder(it)
                builder.setTitle("Выберите лампы")
                    .setMultiChoiceItems(
                        remainingLamps.map { x -> "$x" }.toTypedArray(),
                        checkedLamps
                    ) { dialog, which, isChecked ->
                        checkedLamps[which] = isChecked
                        val name = remainingLamps[which] // Get the clicked item
                        Toast.makeText(activity, "$name", Toast.LENGTH_LONG).show()
                    }
                    .setPositiveButton("Готово") { dialog, id ->
                        // User clicked OK, so save the selectedItems results somewhere
                        for (i in remainingLamps.indices) {
                            val checked = checkedLamps[i]
                            if (checked) {
                                (activity as SceneComponentsActivity).adapter.addLamp(Lamp(remainingLamps[i]))
                                (activity as SceneComponentsActivity).vm.change()
                                Log.i("Dialog", "Added - ${remainingLamps[i]}")
                            }
                        }
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.cancel()
                    }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }
}