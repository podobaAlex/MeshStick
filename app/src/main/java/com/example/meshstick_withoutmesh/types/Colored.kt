package com.example.meshstick_withoutmesh.types

interface Colored : SceneComponents {
    var red: Int
    var green: Int
    var blue: Int

    fun sendToMesh()

}