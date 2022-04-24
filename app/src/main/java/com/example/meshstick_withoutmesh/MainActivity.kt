package com.example.meshstick_withoutmesh

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adapters.RVConnectedMeshesAdapter
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.Mesh
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.myapplication.R
import io.paperdb.Paper

//Начальная сцена
class MainActivity : AppCompatActivity() {

    private val adapter: RVConnectedMeshesAdapter = RVConnectedMeshesAdapter(this)
    var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                    connectedMeshes.addAll(mutableListOf(Mesh("mesh1"), Mesh("mesh2")))
                    connectedMeshes[0].lamps.addAll(mutableListOf(Lamp("eofeoifj"), Lamp("019joisdjf")))
                    connectedMeshes[1].lamps.addAll(mutableListOf(Lamp("eofdfifj"), Lamp("9joisdjf")))
                    adapter.notifyDataSetChanged()
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

}