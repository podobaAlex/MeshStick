package com.example.meshstick_withoutmesh.types

class Mesh(var name: String) {
    var isConnected: Boolean = false
    val lamps = mutableListOf<Lamp>()

}