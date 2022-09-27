package com.example.meshstick_withoutmesh.types

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import kotlinx.coroutines.sync.Mutex
import java.util.*

var scenes = mutableListOf<Scene>()
var connectedMeshes = mutableListOf<Mesh>()
var activeScene: Int = -1
val changedLamps: Stack<Int> = Stack()

lateinit var wifiManager: WifiManager
lateinit var scanResults: MutableList<ScanResult?>

val mutex = Mutex()