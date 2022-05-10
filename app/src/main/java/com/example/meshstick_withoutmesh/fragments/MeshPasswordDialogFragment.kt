package com.example.meshstick_withoutmesh.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.meshstick_withoutmesh.types.wifiManager

class MeshPasswordDialogFragment(private val num: Int) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setTitle("Password")

            val input = EditText(it)
            builder.setView(input)

            builder.setPositiveButton("Save") { dialog, id ->
                run {
                    val mesh = WifiNetworkSuggestion.Builder().setSsid(connectedMeshes[num].name)
                        .setWpa2Passphrase(input.text.toString()).build()
                    val status = wifiManager.addNetworkSuggestions(listOf(mesh))
                    when (status) {
                        WifiManager.STATUS_SUGGESTION_CONNECTION_FAILURE_AUTHENTICATION -> {
                            Toast.makeText(context, "WRONG PASSWORD", Toast.LENGTH_LONG)
                        }
                    }
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}