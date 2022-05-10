package com.example.meshstick_withoutmesh

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adapters.RVConnectedMeshesAdapter
import com.example.meshstick_withoutmesh.types.Mesh
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.meshstick_withoutmesh.types.scanResults
import com.example.meshstick_withoutmesh.types.wifiManager
import com.example.myapplication.R
import io.paperdb.Paper

//Начальная сцена
class MainActivity : AppCompatActivity() {

    private val adapter: RVConnectedMeshesAdapter = RVConnectedMeshesAdapter(this)
    var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val sceneRV = findViewById<RecyclerView>(R.id.rv_lampsConnected)
        sceneRV.layoutManager = LinearLayoutManager(this)
        sceneRV.adapter = adapter

        // инициализируем хранилище
        Paper.init(applicationContext)

        //Переход в ScenesActivity
        val btOpenScenes: Button = findViewById(R.id.bt_openScenes)
        btOpenScenes.setOnClickListener {
            val intent = Intent(this, ScenesActivity::class.java)
            startActivity(intent)
        }

    }

    //Хот-бар
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //Обработка кнопок хот-бара
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //Добавить сцену
            R.id.connect -> {
                isConnected = !isConnected
                if (isConnected) {
                    item.setIcon(R.drawable.connected)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ),
                            PackageManager.PERMISSION_GRANTED
                        )
                        //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overridden method

                    } else {
                        connectToWifi()
                        //do something, permission was previously granted; or legacy device
                    }
                } else {
                    item.setIcon(R.drawable.disconnected)
                    connectedMeshes.clear()
                    adapter.notifyDataSetChanged()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("ShowToast")
    private fun connectToWifi() {

        val state: String = when (wifiManager.wifiState) {
            WifiManager.WIFI_STATE_DISABLING -> "Disabling"
            WifiManager.WIFI_STATE_DISABLED -> "Disabled"
            WifiManager.WIFI_STATE_ENABLING -> "Enabling"
            WifiManager.WIFI_STATE_ENABLED -> "Enabled"
            WifiManager.WIFI_STATE_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }

        Log.d("STATE", state)

        scanResults = wifiManager.scanResults

        Log.d("WI-FI", "size: ${scanResults.size}")

        connectedMeshes = scanResults.map { it -> Mesh(it!!.SSID) }.toMutableList()
        adapter.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == PackageManager.PERMISSION_GRANTED
            && grantResults.all { it -> it == PackageManager.PERMISSION_GRANTED }
        ) {
            connectToWifi()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}