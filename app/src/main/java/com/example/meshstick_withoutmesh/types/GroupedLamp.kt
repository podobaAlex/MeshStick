package com.example.meshstick_withoutmesh.types

import android.os.Parcel
import android.os.Parcelable

class GroupedLamp(override var name: String) : SceneComponents {
    constructor(parcel: Parcel) : this(parcel.readString()!!)

    constructor(lamp: Lamp) : this(lamp.name)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroupedLamp> {
        override fun createFromParcel(parcel: Parcel): GroupedLamp {
            return GroupedLamp(parcel)
        }

        override fun newArray(size: Int): Array<GroupedLamp?> {
            return arrayOfNulls(size)
        }
    }

}