package com.example.meshstick_withoutmesh.mesh

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import com.example.meshstick_withoutmesh.MainActivity
import com.example.meshstick_withoutmesh.mesh.MeshCommunicator.WriteData
import com.example.meshstick_withoutmesh.mesh.MeshCommunicator.isConnected
import com.example.meshstick_withoutmesh.mesh.MeshCommunicator.sendMyBroadcast
import com.example.meshstick_withoutmesh.types.Lamp
import com.example.meshstick_withoutmesh.types.connectedMeshes
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.math.BigInteger
import java.net.NetworkInterface
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Class with handling functions to manage mesh network events and tasks
 * Includes some general utilities as well
 */
class MeshHandler : MainActivity() {
    companion object {
        /** Debug tag  */
        private const val DBG_TAG = "MeshHandler"

        /** Path to OTA file as String  */
        var otaPath: String? = null

        /** Name of file for OTA  */
        var otaFile: File? = null

        /** md5 Checksum of the OTA file  */
        var otaMD5: String? = null

        /** File size  */
        var otaFileSize: Long = 0

        /** Size of one block  */
        private const val otaBlockSize = 1024

        /** Number of blocks for the update  */
        private var numOfBlocks: Long = 0

        /** Selected HW type  */
        private var otaHWtype: String? = null

        /** Selected node type  */
        private var otaNodeType: String? = null

        /**
         * Returns mesh nodeID created from given MAC address.
         * @param macAddress mac address to create the nodeID
         * @return  nodeID or -1
         */
        fun createMeshID(macAddress: String): Long {
            var calcNodeId: Long = -1
            val macAddressParts = macAddress.split(":").toTypedArray()
            if (macAddressParts.size == 6) {
                try {
                    var number = java.lang.Long.valueOf(macAddressParts[2], 16)
                    if (number < 0) {
                        number = number * -1
                    }
                    calcNodeId = number * 256 * 256 * 256
                    number = java.lang.Long.valueOf(macAddressParts[3], 16)
                    if (number < 0) {
                        number = number * -1
                    }
                    calcNodeId += number * 256 * 256
                    number = java.lang.Long.valueOf(macAddressParts[4], 16)
                    if (number < 0) {
                        number = number * -1
                    }
                    calcNodeId += number * 256
                    number = java.lang.Long.valueOf(macAddressParts[5], 16)
                    if (number < 0) {
                        number = number * -1
                    }
                    calcNodeId += number
                } catch (ignore: NullPointerException) {
                    calcNodeId = -1
                }
            }
            return calcNodeId
        }// for now eat exceptions
        // Couldn't get a MAC address, just imagine one
        /*try {
         // this is so Linux hack
         return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
     } catch (IOException ex) {
         return null;
     }*/
        /**
         * Returns MAC address of the given interface name.
         * @return  mac address or fake MAC address
         */
        val wifiMACAddress: String
            get() {
                try {
                    val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
                    for (intf in interfaces) {
                        if (!intf.name.equals("wlan0", ignoreCase = true)) continue
                        val mac = intf.hardwareAddress ?: return ""
                        val buf = StringBuilder()
                        for (aMac in mac) buf.append(String.format("%02X:", aMac))
                        if (buf.length > 0) buf.deleteCharAt(buf.length - 1)
                        return buf.toString()
                    }
                } catch (ignored: Exception) {
                } // for now eat exceptions
                // Couldn't get a MAC address, just imagine one
                return "01:02:03:04:05:06"
                /*try {
                 // this is so Linux hack
                 return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
             } catch (IOException ex) {
                 return null;
             }*/
            }

        /**
         * Send a message to the painlessMesh network
         * @param rcvNode Receiving node Id
         * @param msgToSend Message to send as "msg"
         */
        fun sendNodeMessage(rcvNode: Long, msgToSend: String) {
            if (isConnected) {
                val meshMessage = JSONObject()
                try {
                    var dataSet = logTime()
                    meshMessage.put("dest", rcvNode)
                    meshMessage.put("from", myNodeId)
                    dataSet += if (rcvNode == 0L) {
                        meshMessage.put("type", 8)
                        "Sending Broadcast:\n$msgToSend\n"
                    } else {
                        meshMessage.put("type", 9)
                        "Sending Single Message to :$rcvNode\n$msgToSend\n"
                    }
                    meshMessage.put("msg", msgToSend)
                    val msg = meshMessage.toString()
                    val data = msg.toByteArray()
                    WriteData(data)
                    if (out != null) {
                        try {
                            out!!.append(dataSet)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    Log.d(DBG_TAG, "Sending data $msg")
                } catch (e: JSONException) {
                    Log.e(DBG_TAG, "Error sending data: " + e.message)
                }
            }
        }

        /**
         * Send a node sync request to the painlessMesh network
         */
        fun sendNodeSyncRequest() {
            if (isConnected) {
                var dataSet = logTime()
                dataSet += "Sending NODE_SYNC_REQUEST\n"
                val nodeMessage = JSONObject()
                val subsArray = JSONArray()
                try {
                    nodeMessage.put("dest", apNodeId)
                    nodeMessage.put("from", myNodeId)
                    nodeMessage.put("type", 5)
                    nodeMessage.put("subs", subsArray)
                    val msg = nodeMessage.toString()
                    val data = msg.toByteArray()
                    WriteData(data)
                    if (out != null) {
                        try {
                            out!!.append(dataSet)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    Log.d(DBG_TAG, "Sending node sync request$msg")
                } catch (e: JSONException) {
                    Log.e(DBG_TAG, "Error sending node sync request: " + e.message)
                }
            }
        }

        /**
         * Send a node sync request to the painlessMesh network
         */
        fun sendTimeSyncRequest() {
            if (isConnected) {
                var dataSet = logTime()
                dataSet += "Sending TIME_SYNC_REQUEST\n"
                val nodeMessage = JSONObject()
                val typeObject = JSONObject()
                try {
                    nodeMessage.put("dest", apNodeId)
                    nodeMessage.put("from", myNodeId)
                    nodeMessage.put("type", 4)
                    typeObject.put("type", 0)
                    nodeMessage.put("msg", typeObject)
                    val msg = nodeMessage.toString()
                    val data = msg.toByteArray()
                    WriteData(data)
                    if (out != null) {
                        try {
                            out!!.append(dataSet)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    Log.d(DBG_TAG, "Sending time sync request$msg")
                } catch (e: JSONException) {
                    Log.e(DBG_TAG, "Error sending time sync request: " + e.message)
                }
            }
        }

        /**
         * Prepare OTA file advertisment and send it as broadcast
         * @param hwType 0 == ESP32, 1 == ESP8266
         * @param nodeType String indicating the target node type
         */
        fun sendOTAAdvertise(hwType: Int, nodeType: String?, forcedUpdate: Boolean) {
            numOfBlocks = otaFileSize / otaBlockSize
            val lastBlockSize = otaFileSize - numOfBlocks * otaBlockSize
            // If last block size is not 0, then we need to report 1 more block!
            if (lastBlockSize != 0L) {
                numOfBlocks += 1
            }
            Log.d(
                DBG_TAG, "Filesize = " + otaFileSize
                        + " # of blocks = " + numOfBlocks
                        + " last block size = " + lastBlockSize
            )
            val otaAdvert = JSONObject()
            try {
                otaHWtype = if (hwType == 0) "ESP32" else "ESP8266"
                otaNodeType = nodeType
                otaAdvert.put("plugin", "ota")
                otaAdvert.put("type", "version")
                otaAdvert.put("md5", otaMD5)
                otaAdvert.put("hardware", otaHWtype)
                otaAdvert.put("nodeType", otaNodeType)
                otaAdvert.put("noPart", numOfBlocks)
                otaAdvert.put("forced", forcedUpdate)
                // Send OTA advertisment
                sendNodeMessage(0, otaAdvert.toString())
            } catch (e: JSONException) {
                Log.e(DBG_TAG, "Error sending OTA advertise: " + e.message)
            }
        }

        /**
         * Send the requested block of the OTA file
         * @param rcvNode ID of the requesting node
         * @param partNo Requested block of the OTA file
         */
        fun sendOtaBlock(rcvNode: Long, partNo: Long) {
            // TODO do we need to queue update requests? Network might get very busy if we update several nodes at the same time
            val otaBlock = JSONObject()
            val otaFile: RandomAccessFile
            try {
                otaFile = RandomAccessFile(otaPath, "r")
                otaFile.seek(partNo * otaBlockSize)
                val index = (partNo * otaBlockSize).toInt()
                if (partNo != 0L) {
                    Log.d(DBG_TAG, "Request for part No $partNo")
                }
                otaFile.seek(index.toLong())
                val buffer = ByteArray(otaBlockSize)
                val size = otaFile.read(buffer, 0, otaBlockSize)
                val b64Buffer = Base64.encodeToString(buffer, 0, size, Base64.NO_WRAP)
                Log.d(
                    DBG_TAG,
                    "Sending block " + partNo + " with decoded size " + size + " encoded size " + b64Buffer.length
                )
                otaBlock.put("plugin", "ota")
                otaBlock.put("type", "data")
                otaBlock.put("md5", otaMD5)
                otaBlock.put("hardware", otaHWtype)
                otaBlock.put("nodeType", otaNodeType)
                otaBlock.put("noPart", numOfBlocks)
                otaBlock.put("partNo", partNo)
                otaBlock.put("data", b64Buffer)
                otaBlock.put("dataLength", b64Buffer.length)
                // Send OTA data block
                sendNodeMessage(rcvNode, otaBlock.toString())
            } catch (e: IOException) {
                Log.e(DBG_TAG, "Error sending OTA block " + partNo + " => " + e.message)
            } catch (e: JSONException) {
                Log.e(DBG_TAG, "Error sending OTA block " + partNo + " => " + e.message)
            }
        }

        /**
         * Build a list of known nodes from the received JSON data
         * @param routingInfo String with the JSON data
         */
        fun generateNodeList(routingInfo: String?) {
            try {
                // Creat list if necessary
                val oldNodesList = ArrayList(connectedMeshes[currentMeshNumber!!].lamps)
                oldNodesList.sortBy { it.id }
                connectedMeshes[currentMeshNumber!!].lamps.clear()
                // Start parsing the node list JSON
                try {
                    val routingTop = JSONObject(routingInfo)
                    val from = routingTop.getLong("from")
                    Log.d(DBG_TAG, "$from")
                    if (currentMeshNumber != null) {
                        if (!connectedMeshes[currentMeshNumber!!].lamps.map { it.id }.any { it == from }) {
                            connectedMeshes[currentMeshNumber!!].lamps.add(Lamp(from))
                        }
                    }
                    getSubsNodeId(routingTop)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                Log.d(DBG_TAG, "New nodes list: " + connectedMeshes)
                connectedMeshes[currentMeshNumber!!].lamps.sortBy { it.id }
                val oldEqualNew = oldNodesList.containsAll(connectedMeshes[currentMeshNumber!!].lamps)
                val newEqualOld = connectedMeshes[currentMeshNumber!!].lamps.containsAll(oldNodesList)
                if (!oldEqualNew || !newEqualOld) {
                    val nodesListStr = StringBuilder("Nodeslist changed\n")
                    for (idx in connectedMeshes[currentMeshNumber!!].lamps.indices) {
                        nodesListStr.append(connectedMeshes[currentMeshNumber!!].lamps[idx]).append("\n")
                    }
                    sendMyBroadcast(MeshCommunicator.MESH_NODES, nodesListStr.toString())
                }
            } catch (e: NullPointerException) {
                Log.e(DBG_TAG, "nodesList is null in fun generateNodeList")
            }
        }

        /**
         * Extract "subs" entries from the JSON data
         * Get the nodeId from the "subs" entry
         * Check if there is another "subs" entry within
         * Call itself recursiv until all "subs" are parsed
         * @param test JSON object to work on
         */
        private fun getSubsNodeId(test: JSONObject) {
            try {
                if (hasSubsNode(test)) {
                    var idx = 0
                    var foundNode: Long
                    val subs = test.getJSONArray("subs")
                    // Go through all "subs" and get the node ids
                    do {
                        foundNode = hasSubsNodeId(subs, idx)
                        if (foundNode != 0L) {
                            if (!connectedMeshes[currentMeshNumber!!].lamps.map { it.id }.any { it == foundNode }) {
                                connectedMeshes[currentMeshNumber!!].lamps.add(Lamp(foundNode))
                            }
                        }
                        idx++
                    } while (foundNode != 0L)

                    // Go again through all "subs" and check if there is a "subs" within
                    idx = 0
                    do {
                        try {
                            val subsub = subs.getJSONObject(idx)
                            getSubsNodeId(subsub)
                        } catch (ignore: JSONException) {
                            return
                        }
                        idx++
                    } while (idx <= 10)
                }
            } catch (e: JSONException) {
                Log.d(DBG_TAG, "getSubsNodeId exception - should never happen")
            } catch (e: NullPointerException) {
                Log.e(DBG_TAG, "nodesList is null in fun getSubsNodeId")
            }
        }

        /**
         * Check if a JSON object has a "subs" entry
         * @param test JSON object to test
         * @return  true if "subs" was found, false if no "subs" was found
         */
        private fun hasSubsNode(test: JSONObject): Boolean {
            return try {
                test.getJSONArray("subs")
                true
            } catch (e: JSONException) {
                false
            }
        }

        /**
         * Check if a JSON array has a "nodeId" entry
         * @param test JSON array to test
         * @param index Sub object to test
         * @return nodeID or 0 if no "nodeId" was found
         */
        private fun hasSubsNodeId(test: JSONArray, index: Int): Long {
            return try {
                test.getJSONObject(index).getLong("nodeId")
            } catch (e: JSONException) {
                0
            }
        }

        /**
         * Get time for log output
         * @return String with date/time in format [hh:mm:ss:ms]
         */
        @SuppressLint("DefaultLocale")
        private fun logTime(): String {
            val now = DateTime()
            return String.format(
                "[%02d:%02d:%02d:%03d] ",
                now.hourOfDay,
                now.minuteOfHour,
                now.secondOfMinute,
                now.millisOfSecond
            )
        }

        /**
         * Calculate the md5 checksum of a file
         * @param updateFile File the checksum should be calculated from
         */
        fun calculateMD5(updateFile: File?): String? {
            val digest: MessageDigest
            digest = try {
                MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                Log.e(DBG_TAG, "Exception while getting digest", e)
                return null
            }
            val `is`: InputStream
            `is` = try {
                FileInputStream(updateFile)
            } catch (e: FileNotFoundException) {
                Log.e(DBG_TAG, "Exception while getting FileInputStream", e)
                return null
            }
            val buffer = ByteArray(8192)
            var read: Int
            return try {
                while (`is`.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
                val md5sum = digest.digest()
                val bigInt = BigInteger(1, md5sum)
                var output = bigInt.toString(16)
                // Fill to 32 chars
                output = String.format("%32s", output).replace(' ', '0')
                output
            } catch (e: IOException) {
                throw RuntimeException("Unable to process file for MD5", e)
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    Log.e(DBG_TAG, "Exception on closing MD5 input stream", e)
                }
            }
        }
    }
}