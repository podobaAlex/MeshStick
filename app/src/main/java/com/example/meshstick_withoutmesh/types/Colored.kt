package com.example.meshstick_withoutmesh.types

interface Colored : SceneComponent {
    var red: Int
    var green: Int
    var blue: Int

    fun sendToMesh()

}