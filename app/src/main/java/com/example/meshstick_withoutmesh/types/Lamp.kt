package com.example.meshstick_withoutmesh.types

import android.os.Parcel
import android.os.Parcelable

class Lamp(
    override var name: String,
    override var red: Int,
    override var green: Int,
    override var blue: Int
) : SceneComponents, Colored {

    var id: String = "null"

    constructor(id: String) : this("lamp", 0, 0, 0) {
        this.id = id
    }

    constructor(groupedLamp: GroupedLamp) : this(groupedLamp.name, 0, 0, 0) {
        this.id = groupedLamp.id
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
        id = parcel.readString()!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(red)
        parcel.writeInt(green)
        parcel.writeInt(blue)
        parcel.writeString(id)
    }

    companion object CREATOR : Parcelable.Creator<Lamp> {
        override fun createFromParcel(parcel: Parcel): Lamp {
            return Lamp(parcel)
        }

        override fun newArray(size: Int): Array<Lamp?> {
            return arrayOfNulls(size)
        }
    }


}