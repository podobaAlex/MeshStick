package com.example.meshstick_withoutmesh.types

import android.net.wifi.ScanResult

class Mesh(var name: String) {
    var isConnected: Boolean = false
    var MAC: String = ""
    val lamps = mutableListOf<Lamp>()

    constructor(scanResult: ScanResult) : this(scanResult.SSID) {
        MAC = scanResult.BSSID
    }

}