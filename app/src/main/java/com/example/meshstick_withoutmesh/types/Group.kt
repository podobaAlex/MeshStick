package com.example.meshstick_withoutmesh.types

import android.os.Parcel
import android.os.Parcelable

class Group(
    override var name: String,
    override var red: Int,
    override var green: Int,
    override var blue: Int
) : SceneComponents, Colored {

    var lamps = mutableListOf<GroupedLamp>()
    var expanded: Boolean = true

    constructor(name: String) : this(name, 0, 0, 0)

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
        parcel.readParcelableList(lamps, Lamp::class.java.classLoader)
        expanded = parcel.readBoolean()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(red)
        parcel.writeInt(green)
        parcel.writeInt(blue)
        parcel.writeParcelableList(lamps, 0)
        parcel.writeBoolean(expanded)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Group> {
        override fun createFromParcel(parcel: Parcel): Group {
            return Group(parcel)
        }

        override fun newArray(size: Int): Array<Group?> {
            return arrayOfNulls(size)
        }
    }

}