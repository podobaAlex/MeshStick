package com.example.meshstick_withoutmesh.types

class Lamp(private var name: String) {
    var red: Int = 0
    var green: Int = 0
    var blue: Int = 0

    constructor(name: String, red: Int, green: Int, blue: Int) : this(name) {
        this.red = red
        this.green = green
        this.blue = blue
    }

    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }
}