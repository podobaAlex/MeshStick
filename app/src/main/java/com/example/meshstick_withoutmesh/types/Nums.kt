package com.example.meshstick_withoutmesh.types

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager

var scenes = mutableListOf<Scene>()
var connectedMeshes = mutableListOf<Mesh>()

lateinit var wifiManager: WifiManager
lateinit var scanResults: MutableList<ScanResult?>