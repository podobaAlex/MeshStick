package com.example.meshstick_withoutmesh

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adapters.RVConnectedLampsAdapter
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.connectedLamps
import com.example.myapplication.R
import io.paperdb.Paper

//Начальная сцена
class MainActivity : AppCompatActivity() {

    val adapter: RVConnectedLampsAdapter = RVConnectedLampsAdapter()
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
                    connectedLamps.addAll(
                        mutableListOf(
                            Lamp("9184u71987449"),
                            Lamp("0wjfie09r30"),
                            Lamp("0239ruwjefji2")
                        )
                    )
                    adapter.notifyDataSetChanged()
                } else {
                    item.setIcon(R.drawable.disconnected)
                    connectedLamps.clear()
                    adapter.notifyDataSetChanged()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}