package com.example.meshstick_withoutmesh.types

class Lamp() : SceneComponents() {

    constructor(name: String) : this() {
        this.name = name
    }

    constructor(name: String, red: Int, green: Int, blue: Int) : this() {
        this.name = name
        this.red = red
        this.green = green
        this.blue = blue
    }

}