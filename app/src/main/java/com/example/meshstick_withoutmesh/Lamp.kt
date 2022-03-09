package com.example.meshstick_withoutmesh

class Lamp (name: String) {
    private lateinit var name : String
    public var red : Int = 0
    public var green: Int = 0
    public var blue: Int = 0

    init {
        this.name = name
    }

    fun getName() : String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }
}