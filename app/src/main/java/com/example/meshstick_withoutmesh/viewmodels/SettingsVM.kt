package com.example.meshstick_withoutmesh.viewmodels

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsVM(
    private var red: Int,
    private var green: Int,
    private var blue: Int
) : ViewModel() {

    private val VMresult = MutableLiveData<Int>()
    var result: LiveData<Int> = VMresult

    fun saveColor(
        red: Int = this.red,
        green: Int = this.green,
        blue: Int = this.blue
    ) {
        this.red = red
        this.green = green
        this.blue = blue
        VMresult.value = Color.rgb(red, green, blue)
    }

}