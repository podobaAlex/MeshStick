package com.example.meshstick_withoutmesh

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meshstick_withoutmesh.adapters.RVConnectedMeshesAdapter
import com.example.meshstick_withoutmesh.mesh.MeshCommunicator
import com.example.meshstick_withoutmesh.mesh.MeshHandler
import com.example.meshstick_withoutmesh.types.Mesh
import com.example.meshstick_withoutmesh.types.connectedMeshes
import com.example.meshstick_withoutmesh.types.scanResults
import com.example.meshstick_withoutmesh.types.wifiManager
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import io.paperdb.Paper
import org.joda.time.DateTime
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

//Начальная сцена
open class MainActivity : AppCompatActivity() {

    var isScanned: Boolean = false

    private val adapter: RVConnectedMeshesAdapter = RVConnectedMeshesAdapter(this)

    private var oldAPName = ""
    private var filterId: Long = 0
    private var logFilePath: String? = null

    lateinit var nameCurrentMesh: String

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

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            var timeForNodeReq = true
            override fun run() {
                if (MeshCommunicator.isConnected) { // brakes are removed everywhere isConnected() -> isConnected
                    timeForNodeReq = if (timeForNodeReq) {
                        MeshHandler.sendNodeSyncRequest()
                        false
                    } else {
                        MeshHandler.sendTimeSyncRequest()
                        true
                    }
                }
                handler.postDelayed(this, 10000)
            }
        }, 10000)

    }

    private val DEFAULT_PORT = "5555"

    override fun onResume() {
        // Register Mesh events

        meshPort = Integer.valueOf(DEFAULT_PORT)

        val intentFilter = IntentFilter()
        intentFilter.addAction(MeshCommunicator.MESH_DATA_RECVD)
        intentFilter.addAction(MeshCommunicator.MESH_SOCKET_ERR)
        intentFilter.addAction(MeshCommunicator.MESH_CONNECTED)
        intentFilter.addAction(MeshCommunicator.MESH_NODES)
        intentFilter.addAction(MeshCommunicator.MESH_OTA)
        intentFilter.addAction(MeshCommunicator.MESH_OTA_REQ)
        // Register network change events
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        // Register receiver
        registerReceiver(localBroadcastReceiver, intentFilter)
        super.onResume()
    }


    override fun onStop() {
        Log.d(DBG_TAG, "Stopped")
        super.onStop()
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
                isScanned = !isScanned
                if (isScanned) {
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
                    stopConnection()
                    connectedMeshes.clear()
                    adapter.notifyDataSetChanged()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun handleConnection(num: Int, meshPW: String) {
        if (!isConnected) {
            if (tryToConnect) {
                stopConnection()
            } else {
                Log.d(DBG_TAG, "startConnectionRequest")
                startConnectionRequest(num, meshPW)
            }
        } else {
            stopConnection()
        }
    }


    private fun startConnectionRequest(num: Int, meshPW: String) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        tryToConnect = true
        userDisConRequest = false

        // Get current active WiFi AP
        oldAPName = ""
        nameCurrentMesh = connectedMeshes[num].name
        currentMeshNumber = num
        // Get current WiFi connection
        try {
            val connManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (networkInfo!!.isConnected) {
                val connectionInfo = wifiManager.connectionInfo
                if (connectionInfo != null && !connectionInfo.ssid.isEmpty()) {
                    oldAPName = connectionInfo.ssid
                }
            }

            // Add device AP to network list and enable it
            val meshAPConfig = WifiConfiguration()
            meshAPConfig.SSID = "\"" + nameCurrentMesh + "\""
            meshAPConfig.preSharedKey = "\"" + meshPW + "\""
            Log.d(DBG_TAG, "SSID = $nameCurrentMesh")
            Log.d(DBG_TAG, "PW = $meshPW")
            val newId = wifiManager.addNetwork(meshAPConfig)
            if (BuildConfig.DEBUG) Log.i(DBG_TAG, "Result of addNetwork: $newId")
            wifiManager.disconnect()
            if (wifiManager.enableNetwork(newId, true)) {
                connectedMeshes[num].isConnected = true
                adapter.notifyItemChanged(num)
                Log.d(DBG_TAG, "CONNECT")
            } else {
                Log.d(DBG_TAG, "WRONG PASSWORD")
            }
            wifiManager.reconnect()
        } catch (e: NullPointerException) {
            Log.e(DBG_TAG, "wifiMgr is null")
        }
    }


    private fun stopConnection() {
        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (MeshCommunicator.isConnected) {
                MeshCommunicator.Disconnect()
            }
            isConnected = false
            tryToConnect = false
            userDisConRequest = true
            val availAPs = if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PackageManager.PERMISSION_GRANTED
                )
                return
            } else wifiManager.configuredNetworks
            if (oldAPName.isEmpty()) {
                for (index in availAPs.indices) {
                    if (connectedMeshes.any {
                            if (it.isConnected) it.name == availAPs[index].SSID
                            else false
                        }) {
                        wifiManager.disconnect()
                        wifiManager.disableNetwork(availAPs[index].networkId)
                        if (BuildConfig.DEBUG) Log.d(DBG_TAG, "Disabled: " + availAPs[index].SSID)
                        wifiManager.reconnect()
                        break
                    }
                }
            } else {
                for (index in availAPs.indices) {
                    if (availAPs[index].SSID.equals(oldAPName, ignoreCase = true)) {
                        wifiManager.disconnect()
                        wifiManager.enableNetwork(availAPs[index].networkId, true)
                        if (BuildConfig.DEBUG) Log.d(DBG_TAG, "Re-enabled: " + availAPs[index].SSID)
                        wifiManager.reconnect()
                        connectedMeshes.find { it.name == availAPs[index].SSID }!!.isConnected = true
                        adapter.notifyDataSetChanged()
                        break
                    }
                }
            }
            stopLogging()
        } catch (e: NullPointerException) {
            Log.e(DBG_TAG, "null in fun stopConnection")
        }
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

        connectedMeshes = scanResults.map { it -> Mesh(it!!) }.toMutableList()
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

    private fun startLogging() {
        if (doLogging) {
            stopLogging()
        }
        val now = DateTime()
        val logTitle = String.format(
            "Log created: %02d/%02d/%02d %02d:%02d\n\n", now.year - 2000, now.monthOfYear,
            now.dayOfMonth, now.hourOfDay, now.minuteOfHour
        )
        /* Name of the log */
        val logName = "meshLogFile.txt"

        // Create folder for this data set
        try {
            val appDir = File(sdcardPath)
            val exists = appDir.exists()
            if (!exists) {
                val result = appDir.mkdirs()
                if (!result) {
                    Log.d(DBG_TAG, "Failed to create log folder")
                }
            }
        } catch (exc: Exception) {
            Log.e(DBG_TAG, "Failed to create log folder: $exc")
        }

        // If file is still open for writing, close it first
        if (out != null) {
            try {
                out!!.flush()
                out!!.close()
            } catch (exc: IOException) {
                Log.e(DBG_TAG, "Failed to close log file: $exc")
            }
        }

        // TODO find a better solution to handle the log files
        // For now delete the old log file to avoid getting a too large file
        var result = File(sdcardPath + logName).exists()
        if (result) {
            result = File(sdcardPath + logName).delete()
            if (!result) {
                Log.d(DBG_TAG, "Failed to delete the old logfile")
            }
        }
        try {
            logFilePath = sdcardPath + logName
            val newFile = FileWriter(logFilePath)
            out = BufferedWriter(newFile)
            out!!.append(logTitle)
        } catch (exc: IOException) {
            Log.e(DBG_TAG, "Failed to open log file for writing: $exc")
        } catch (e: NullPointerException) {
            Log.e(DBG_TAG, "out is null in onActivityResult")
        }
        doLogging = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MeshCommunicator.isConnected) {
            MeshCommunicator.Disconnect()
        }
        stopLogging()
        // unregister the broadcast receiver
        unregisterReceiver(localBroadcastReceiver)
    }

    private fun stopLogging() {
        if (doLogging) {
            if (out != null) {
                try {
                    out!!.flush()
                    out!!.close()
                } catch (exc: IOException) {
                    Log.e(DBG_TAG, "Failed to close log file: $exc")
                }
                out = null
            }
            doLogging = false
            val toBeScannedStr = arrayOfNulls<String>(1)
            toBeScannedStr[0] = sdcardPath + "*"
            MediaScannerConnection.scanFile(this, toBeScannedStr, null) { path: String, uri: Uri? ->
                println(
                    "SCAN COMPLETED: $path"
                )
            }
        }
    }

    private val localBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.M)
        @SuppressLint("DefaultLocale", "ServiceCast")
        override fun onReceive(context: Context, intent: Intent) {
            // Connection change
            val intentAction = intent.action
            Log.d(DBG_TAG, "Received broadcast: $intentAction")
            // WiFi events
            if (isConnected) {
                // Did we loose connection to the mesh network?
                /* Access to connectivity manager */
                val cm = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                /* WiFi connection information  */

                // if (cm != null) was removed by Dima
                Log.d(DBG_TAG, "${cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)}")
                val wifiOn: NetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!
                if (!wifiOn.isConnected) {
                    isConnected = false
                    runOnUiThread { stopConnection() }
                }
            }
            if (tryToConnect && intentAction != null && intentAction == ConnectivityManager.CONNECTIVITY_ACTION) {
                /* Access to connectivity manager */
                Log.d(DBG_TAG, "Connected1")
                val cm = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                /* WiFi connection information  */
                val wifiOn: NetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!
                Log.d(DBG_TAG, "Connected to Mesh network " + wifiOn.extraInfo)
                if (wifiOn.isConnected) {
                    Log.d(DBG_TAG, "Connected2")
                    if (tryToConnect) {
                        Log.d(DBG_TAG, "Connected3")
                        try {
                            val wifiInfo = wifiManager.connectionInfo
                            if (wifiInfo.ssid.equals("\"" + nameCurrentMesh + "\"", ignoreCase = true)) {
                                Log.d(DBG_TAG, "Connected to Mesh network " + wifiOn.extraInfo)
                                // Get the gateway IP address
                                val dhcpInfo: DhcpInfo
                                if (wifiManager != null) {
                                    // Create the mesh AP node ID from the AP MAC address
                                    apNodeId = MeshHandler.createMeshID(wifiInfo.bssid)
                                    dhcpInfo = wifiManager.dhcpInfo
                                    // Get the mesh AP IP
                                    var meshIPasNumber = dhcpInfo.gateway
                                    meshIP = (meshIPasNumber and 0xFF).toString() + "." +
                                            (8.let {
                                                meshIPasNumber = meshIPasNumber ushr it; meshIPasNumber
                                            } and 0xFF) + "." +
                                            (8.let {
                                                meshIPasNumber = meshIPasNumber ushr it; meshIPasNumber
                                            } and 0xFF) + "." +
                                            (meshIPasNumber ushr 8 and 0xFF)

                                    // Create our node ID
                                    myNodeId = MeshHandler.createMeshID(MeshHandler.wifiMACAddress)
                                } else {
                                    // We are screwed. Tell user about the problem
                                    Log.e(DBG_TAG, "Critical Error -- cannot get WifiManager access")
                                }
                                // Rest has to be done on UI thread
                                runOnUiThread {
                                    tryToConnect = false
                                    val connMsg = "ID: " + myNodeId + " on " + nameCurrentMesh
//                                    tv_mesh_conn?.text = connMsg

                                    // Set flag that we are connected
                                    isConnected = true
                                    startLogging()

                                    // Connected to the Mesh network, start network task now
                                    try {
                                        MeshCommunicator.Connect(meshIP!!, meshPort, applicationContext) // added !!
                                    } catch (e: NullPointerException) {
                                        Log.e(DBG_TAG, "meshIP is null in fun onRecieve")
                                    }
                                }
                            } else {
                                val availAPs = if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    ActivityCompat.requestPermissions(
                                        context as Activity,
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ),
                                        PackageManager.PERMISSION_GRANTED
                                    )
                                    return
                                } else wifiManager.configuredNetworks
                                for (index in availAPs.indices) {
                                    if (availAPs[index].SSID.equals("\"" + nameCurrentMesh + "\"", ignoreCase = true)) {
                                        wifiManager.disconnect()
                                        wifiManager.enableNetwork(availAPs[index].networkId, true)
                                        if (BuildConfig.DEBUG) Log.d(
                                            DBG_TAG,
                                            "Retry to enable: " + availAPs[index].SSID
                                        )
                                        wifiManager.reconnect()
                                        break
                                    }
                                }
                            }
                        } catch (e: NullPointerException) {
                            Log.e(DBG_TAG, "wifiMgr is null in fun onRecieve")
                        }
                    }
                }
            }
            var dataSet: String
            val now = DateTime()
            dataSet = String.format(
                "[%02d:%02d:%02d:%03d] ",
                now.hourOfDay,
                now.minuteOfHour,
                now.secondOfMinute,
                now.millisOfSecond
            )

            // Mesh events
            if (MeshCommunicator.MESH_DATA_RECVD == intentAction) {
                val rcvdMsg = intent.getStringExtra("msg")
                var oldText: String
                try {
                    val rcvdJSON = JSONObject(rcvdMsg)
                    val msgType = rcvdJSON.getInt("type")
                    val fromNode = rcvdJSON.getLong("from")
                    when (msgType) {
                        3 -> {
//                            tv_mesh_err?.text = getString(R.string.mesh_event_time_delay)
                            dataSet += "Received TIME_DELAY\n"
                        }
                        4 -> {
//                            tv_mesh_err?.text = getString(R.string.mesh_event_time_sync)
                            dataSet += "Received TIME_SYNC\n"
                        }
                        5, 6 -> {
                            if (msgType != 5) {
//                                tv_mesh_err?.text = getString(R.string.mesh_event_node_reply)
                                dataSet += "Received NODE_SYNC_REPLY\n"
                            } else {
//                                tv_mesh_err?.text = getString(R.string.mesh_event_node_req)
                                dataSet += "Received NODE_SYNC_REQUEST\n"
                            }
                            // Generate known nodes list
                            val handler = Handler()
                            handler.post {
                                MeshHandler.generateNodeList(rcvdMsg)
                                adapter.notifyDataSetChanged()
                            }
                        }
                        7 -> dataSet += "Received CONTROL\n"
                        8 -> {
                            dataSet += """
                                Broadcast:
                                ${rcvdJSON.getString("msg")}
                                
                                """.trimIndent()
                            if (filterId != 0L) {
                                if (fromNode != filterId) {
                                    return
                                }
                            }
                            oldText = """BC from $fromNode ${rcvdJSON.getString("msg")}"""
//                            tv_mesh_msgs?.append(oldText)
                        }
                        9 -> {
                            dataSet += """
                                Single Msg:
                                ${rcvdJSON.getString("msg")}
                                
                                """.trimIndent()
                            // Check if the message is a OTA req message
                            val rcvdData = JSONObject(rcvdJSON.getString("msg"))
                            var dataType = rcvdData.getString("plugin")
                            if (dataType != null && dataType.equals("ota", ignoreCase = true)) {
                                dataType = rcvdData.getString("type")
                                if (dataType != null) {
                                    if (dataType.equals("version", ignoreCase = true)) {
                                        // We received a OTA advertisment!
//                                        tv_mesh_err?.text = getString(R.string.mesh_event_ota_adv)
                                        return
                                    } else if (dataType.equals("request", ignoreCase = true)) {
                                        // We received a OTA block request
                                        MeshHandler.sendOtaBlock(fromNode, rcvdData.getLong("partNo"))
//                                        tv_mesh_err?.text = getString(R.string.mesh_event_ota_req)
                                    }
                                }
                            }
                            if (filterId != 0L) {
                                if (fromNode != filterId) {
                                    return
                                }
                            }
                            oldText = """SM from $fromNode${rcvdJSON.getString("msg")}"""
//                            tv_mesh_msgs?.append(oldText)
                        }
                    }
                } catch (e: JSONException) {
                    Log.d(DBG_TAG, "Received message is not a JSON Object!")
                    oldText = """
                        E: ${intent.getStringExtra("msg")}
                        
                        """.trimIndent()
//                    tv_mesh_msgs?.append(oldText)
                    dataSet += """
                        ERROR INVALID DATA:
                        ${intent.getStringExtra("msg")}
                        
                        """.trimIndent()
                }
                if (out != null) {
                    try {
                        out!!.append(dataSet)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
//                scrollViewDown()
            } else if (MeshCommunicator.MESH_SOCKET_ERR == intentAction) {
                if (MeshHandler.nodesList != null) {
                    MeshHandler.nodesList!!.clear()
                }
                if (!userDisConRequest) {
                    try {
//                        showToast(getString(R.string.mesh_lost_connection), Toast.LENGTH_LONG)
                        MeshCommunicator.Connect(meshIP!!, meshPort, applicationContext) // added !!
//                        tv_mesh_err?.text = intent.getStringExtra("msg")
                    } catch (e: NullPointerException) {
                        Log.e(DBG_TAG, "meshIP is null in fun onRecieve")
                    }
                }
            } else if (MeshCommunicator.MESH_CONNECTED == intentAction) {
                userDisConRequest = false
            } else if (MeshCommunicator.MESH_NODES == intentAction) {
                val oldText = """
                    ${intent.getStringExtra("msg")}
                    
                    """.trimIndent()
//                tv_mesh_msgs?.append(oldText)
//                scrollViewDown()
                dataSet += """
                    ${intent.getStringExtra("msg")}
                    
                    """.trimIndent()
                if (out != null) {
                    try {
                        out!!.append(dataSet)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
//            var oldText = tv_mesh_msgs?.text.toString()
//            // Check if the text is getting too long
//            if (oldText.length > 16535) {
//                // Quite long, remove the first 20 lines  from the text
//                var indexOfCr = 0
//                for (lines in 0..19) {
//                    indexOfCr = oldText.indexOf("\n", indexOfCr + 1)
//                }
//                oldText = oldText.substring(indexOfCr + 1)
//                tv_mesh_msgs?.text = oldText
//            }
        }
    }

    companion object {
        /** Tag for debug messages of service */
        private const val DBG_TAG = "MainActivity"

        /** Flag if we try to connect to Mesh  */
        private var tryToConnect = false

        var currentMeshNumber: Int? = null

        /** Flag if connection to Mesh was started  */
        private var isConnected = false

        /** Flag when user stops connection  */
        private var userDisConRequest = false

        /** Mesh port == TCP port number  */
        private var meshPort = 0

        /** Mesh network entry IP  */
        private var meshIP: String? = null

        /** My Mesh node id  */
        @JvmField
        var myNodeId: Long = 0

        /** The node id we connected to  */
        @JvmField
        var apNodeId: Long = 0

        /** Flag if log file should be written  */
        private var doLogging = true

        /** For log file of data  */
        @JvmField
        var out: BufferedWriter? = null

        /** Path to storage folder  */
        private var sdcardPath: String? = null
    }
}