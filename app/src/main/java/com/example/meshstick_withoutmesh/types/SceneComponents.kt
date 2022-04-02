package com.example.meshstick_withoutmesh.types

import android.os.Parcelable

interface SceneComponents : Parcelable {

    var name: String
    var red: Int
    var green: Int
    var blue: Int

}