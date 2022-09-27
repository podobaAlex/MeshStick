package com.example.meshstick_withoutmesh.mesh

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.activeScene
import com.example.meshstick_withoutmesh.types.changedLamps
import com.example.meshstick_withoutmesh.types.scenes
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

/**
 * Class to handle the TCP connection and communication
 * with the mesh network
 */
internal object MeshCommunicator {
    /** Debug tag  */
    private const val DBG_TAG = "MeshCommunicator"

    /** Action for MESH data arrived  */
    const val MESH_DATA_RECVD = "DATA"

    /** Action for socket error  */
    const val MESH_SOCKET_ERR = "DISCON"

    /** Action for connection success  */
    const val MESH_CONNECTED = "CON"

    /** Action for received nodes list  */
    const val MESH_NODES = "NODE"

    /** Action for ready for announcing OTA  */
    const val MESH_OTA = "OTA_START"

    /** Action for OTA block request  */
    const val MESH_OTA_REQ = "OTA_REQ"

    /** Flag if the TCP receiving thread was started  */
    private var receiveThreadRunning = false

    /** Socket for the communication with the mesh network  */
    private var connectionSocket: Socket? = null

    //Runnables for sending and receiving data
    private var sendRunnable: SendRunnable? = null

    //Threads to execute the sending
    private var sendThread: Thread? = null

    //Threads to execute the receiving
    private var receiveThread: Thread? = null

    /** Local copy of the mesh network AP gateway address  */
    private var severIp = "192.168.0.2"

    /** Local copy of the mesh network communication port  */
    private var serverPort = 1234

    /** Application context required for broadcast messages  */
    private var appContext: Context? = null

    /**
     * Returns true if MeshCommunicator is connected, else false
     * @return Boolean
     */
    val isConnected: Boolean
        get() = connectionSocket != null && connectionSocket!!.isConnected && !connectionSocket!!.isClosed


    /**
     * Open connection to mesh network node
     */
    fun Connect(ip: String, port: Int, thisContext: Context?) {
        severIp = ip
        serverPort = port
        appContext = thisContext
        Thread(ConnectRunnable()).start()
    }

    /**
     * Close connection to mesh network node
     */
    fun Disconnect() {
        stopThreads()
        try {
            connectionSocket!!.close()
            Log.d(DBG_TAG, "Disconnected!")
        } catch (e: IOException) {
            Log.e(DBG_TAG, "Disconnect failed: " + e.message)
        } catch (e: NullPointerException) {
            Log.e(DBG_TAG, "connectionSocket is null if fun Disconnect")
        }
    }

    /**
     * Send data to mesh network
     * @param data byte array to send
     */
    @JvmStatic
    fun WriteData(data: ByteArray?) {
        if (isConnected) {
            startSending()
            try {
                sendRunnable!!.Send(data)
            } catch (e: NullPointerException) {
                Log.e(DBG_TAG, "sendRunnable is null in fun WriteData")
            }
        }
    }

    /**
     * Stop the receiving and sending threads
     */
    private fun stopThreads() {
        if (receiveThread != null) receiveThread!!.interrupt()
        if (sendThread != null) sendThread!!.interrupt()
    }

    /**
     * Start the thread for sending data
     */
    private fun startSending() {
        sendRunnable = SendRunnable(connectionSocket)
        sendThread = Thread(sendRunnable)
        try {
            sendThread!!.start()
        } catch (e: NullPointerException) {
            Log.e(DBG_TAG, "sendThread is null in fun startSending")
        }
    }

    /**
     * Start the thread for receiving data
     */
    private fun startReceiving() {
        val receiveRunnable = ReceiveRunnable(connectionSocket)
        receiveThread = Thread(receiveRunnable)
        try {
            receiveThread!!.start()
        } catch (e: NullPointerException) {
            Log.e(DBG_TAG, "receiveThread is null in fun startReceiving")
        }
    }

    /**
     * Send received message to all listing threads
     * @param action Broadcast action to be sent
     * @param msgReceived Received data or error message
     */
    @JvmStatic
    fun sendMyBroadcast(action: String?, msgReceived: String?) {
        /* Intent for activity internal broadcast messages */
        val broadCastIntent = Intent()
        broadCastIntent.action = action
        broadCastIntent.putExtra("msg", msgReceived)
        try {
            appContext!!.sendBroadcast(broadCastIntent)
        } catch (e: NullPointerException) {
            Log.e(DBG_TAG, "appContext is null in fun sendMyBroadcast")
        }
    }

    /**
     * Runnable handling receiving data from the mesh network
     */
    internal class ReceiveRunnable(sock: Socket?) : Runnable {
        private var input: InputStream? = null

        init {
            try {
                input = sock!!.getInputStream()
            } catch (e: Exception) {
                Log.e(DBG_TAG, "ReceiveRunnable failed: " + e.message)
            } catch (e: NullPointerException) {
                Log.e(DBG_TAG, "sock is null in fun ReceiveRunnable")
            }
        }

        override fun run() {
            Log.d(DBG_TAG, "Receiving started")
            while (!Thread.currentThread().isInterrupted && isConnected) {
                if (!receiveThreadRunning) receiveThreadRunning = true
                try {
                    val buffer = ByteArray(8192)
                    //Read the first integer, it defines the length of the data to expect
                    val readLen = input!!.read(buffer, 0, buffer.size)
                    if (readLen > 0) {
                        val data = ByteArray(readLen)
                        System.arraycopy(buffer, 0, data, 0, readLen)
                        data[readLen - 1] = 0
                        var rcvdMsg = String(data, StandardCharsets.UTF_8)
                        val realLen = rcvdMsg.lastIndexOf("}")
                        rcvdMsg = rcvdMsg.substring(0, realLen + 1)
                        Log.i(DBG_TAG, "Received $readLen bytes: $rcvdMsg")
                        Log.i(DBG_TAG, "Data received!")

                        if (rcvdMsg.contains("changes:")) {
                            val colorData = rcvdMsg.substring(rcvdMsg.lastIndexOf("changes") + 9, realLen - 1)
                            Log.d("color", colorData)
                            val rgbArray = colorData.split(' ').map { it.toInt() }.toTypedArray()
                            if (activeScene != -1) {
                                val start = rcvdMsg.lastIndexOf("from") + 6
                                val id = rcvdMsg.substring(start, rcvdMsg.lastIndexOf(",")).toLong()
                                val position = scenes[activeScene].sceneComponents.indexOfFirst {
                                    if (it is Lamp) it.id == id else false
                                }

                                if (position != -1) {
                                    (scenes[activeScene].sceneComponents[position] as Lamp).red = rgbArray[0]
                                    (scenes[activeScene].sceneComponents[position] as Lamp).green = rgbArray[1]
                                    (scenes[activeScene].sceneComponents[position] as Lamp).blue = rgbArray[2]
                                    changedLamps.push(position)
                                }
                            }
                        }

                        sendMyBroadcast(MESH_DATA_RECVD, rcvdMsg)
                    }
                } catch (e: IOException) {
                    Log.e(DBG_TAG, "Receiving loop stopped: " + e.message)
                    Disconnect() //Gets stuck in a loop if we don't call this on error!
                    sendMyBroadcast(MESH_SOCKET_ERR, e.message)
                } catch (e: NullPointerException) {
                    Log.e(DBG_TAG, "input is null in fun run")
                }
            }
            receiveThreadRunning = false
            Log.d(DBG_TAG, "Receiving stopped")
        }
    }

    /**
     * Runnable to send data to the mesh network
     */
    internal class SendRunnable(server: Socket?) : Runnable {
        var data: ByteArray? = null
        private var out: OutputStream? = null
        private var hasMessage = false

        init {
            try {
                out = server!!.getOutputStream()
            } catch (e: IOException) {
                Log.e(DBG_TAG, "Start SendRunnable failed: " + e.message)
            } catch (e: NullPointerException) {
                Log.e(DBG_TAG, "server is null in fun SendRunnable")
            }
        }

        /**
         * Send data as bytes to the server
         * @param bytes Data to send
         */
        fun Send(bytes: ByteArray?) {
            data = bytes
            hasMessage = true
        }

        override fun run() {
            Log.d(DBG_TAG, "Sending started")
            if (hasMessage) {
                try {
                    //Send the data
                    out!!.write(data, 0, data!!.size)
                    out!!.write(0)
                    //Flush the stream to be sure all bytes has been written out
                    out!!.flush()
                } catch (e: IOException) {
                    Log.e(DBG_TAG, "Sending failed: " + e.message)
                    Disconnect() //Gets stuck in a loop if we don't call this on error!
                    sendMyBroadcast(MESH_SOCKET_ERR, e.message)
                } catch (e: NullPointerException) {
                    Log.e(DBG_TAG, "out or data is null in fun run")
                }
                hasMessage = false
                data = null
                Log.i(DBG_TAG, "Command has been sent!")
            }
            Log.i(DBG_TAG, "Sending stopped")
        }
    }

    /**
     * Runnable to handle the connection to the mesh network
     */
    internal class ConnectRunnable : Runnable {
        override fun run() {
            try {
                Log.d(DBG_TAG, "C: Connecting...")
                val serverAddr = InetAddress.getByName(severIp)
                //Create a new instance of Socket
                connectionSocket = Socket()
                connectionSocket!!.keepAlive = true
                connectionSocket!!.receiveBufferSize = 32768
                connectionSocket!!.sendBufferSize = 32768
                connectionSocket!!.reuseAddress = true

                // Experimental
                connectionSocket!!.trafficClass = 0x04

                //Start connecting to the server with 5000ms timeout
                //This will block the thread until a connection is established
                connectionSocket!!.connect(InetSocketAddress(serverAddr, serverPort), 5000)
                Log.d(DBG_TAG, "Connected!")

                // Start receiving data now
                startReceiving()
                // Request a list of known nodes
                MeshHandler.sendNodeSyncRequest()
            } catch (e: Exception) {
                Log.e(DBG_TAG, "Connecting failed: " + e.message)
            } catch (e: NullPointerException) {
                Log.e(DBG_TAG, "connectionSocket is null in fun run class ConnectRunnable")
            }
            sendMyBroadcast(MESH_CONNECTED, "")
            Log.i(DBG_TAG, "Connection thread finished")
        }
    }
}