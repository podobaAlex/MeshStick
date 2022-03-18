package com.example.meshstick_withoutmesh.types

class Scene(private var name: String) {
    var sceneComponents = mutableListOf<SceneComponents>()

    constructor() : this("Scene")

    fun getName(): String {
        return this.name
    }

    fun setName(name: String) {
        this.name = name
    }

}