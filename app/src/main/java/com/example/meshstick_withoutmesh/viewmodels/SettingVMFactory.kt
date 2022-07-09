package com.example.meshstick_withoutmesh.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingVMFactory(private var red: Int, private var green: Int, private var blue: Int) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsVM(red, green, blue) as T
    }
}