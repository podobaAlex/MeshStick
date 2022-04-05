package com.example.meshstick_withoutmesh.types

import android.os.Parcelable

interface Colored : Parcelable {
    var red: Int
    var green: Int
    var blue: Int
}