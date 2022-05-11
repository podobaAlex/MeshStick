package com.example.meshstick_withoutmesh.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.net.wifi.WifiConfiguration
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.meshstick_withoutmesh.types.wifiManager
import com.example.myapplication.BuildConfig

class MeshPasswordDialogFragment(private val num: Int) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setTitle("Password")

            val input = EditText(it)
            builder.setView(input)

            builder.setPositiveButton("Save") { dialog, id ->
                run {
                    val mesh = WifiConfiguration()
                    mesh.SSID = connectedMeshes[num].name
                    mesh.preSharedKey = input.text.toString()
                    val newId = wifiManager.addNetwork(mesh)
                    if (BuildConfig.DEBUG) Log.i("DBG_TAG", "Result of addNetwork: $newId")
                    wifiManager.disconnect()
                    wifiManager.enableNetwork(newId, true)
                    wifiManager.reconnect()

//                    val mesh = WifiNetworkSuggestion.Builder().setSsid(connectedMeshes[num].name)
//                        .setWpa2Passphrase(input.text.toString()).build()
//                    val status = wifiManager.addNetworkSuggestions(listOf(mesh))
//                    when (status) {
//                        WifiManager.STATUS_SUGGESTION_CONNECTION_FAILURE_AUTHENTICATION -> {
//                            Toast.makeText(context, "WRONG PASSWORD", Toast.LENGTH_LONG)
//                        }
//                    }
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}