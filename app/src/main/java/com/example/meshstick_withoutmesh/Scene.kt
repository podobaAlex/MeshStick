package com.example.meshstick_withoutmesh



class Scene (name: String){
    private lateinit var name: String
    public var lamps = mutableListOf<Lamp>()
    init {
        this.name = name
    }

    constructor() : this("Scene") {}

    public fun getName() : String {
        return this.name
    }

    public fun setName(name: String) {
        this.name = name
    }

}