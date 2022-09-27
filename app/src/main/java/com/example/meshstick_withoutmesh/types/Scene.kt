package com.example.meshstick_withoutmesh.types

import android.os.Parcel
import android.os.Parcelable

class Scene(private var name: String) : Parcelable {

    var sceneComponents = mutableListOf<SceneComponent>()
    var isActive: Boolean = false

    constructor(parcel: Parcel) : this(parcel.readString()!!) {
        parcel.readParcelableList(sceneComponents, SceneComponent::class.java.classLoader)
    }

    constructor() : this("Scene")

    fun getName(): String {
        return this.name
    }

    fun setName(name: String) {
        this.name = name
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelableList(sceneComponents, flags)
    }

    companion object CREATOR : Parcelable.Creator<Scene> {
        override fun createFromParcel(parcel: Parcel): Scene {
            return Scene(parcel)
        }

        override fun newArray(size: Int): Array<Scene?> {
            return arrayOfNulls(size)
        }
    }

}