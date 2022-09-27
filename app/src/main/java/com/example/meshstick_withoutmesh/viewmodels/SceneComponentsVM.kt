package com.example.meshstick_withoutmesh.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SceneComponentsVM : ViewModel() {

    private val VMresult = MutableLiveData<Boolean>()
    var result: LiveData<Boolean> = VMresult

    init {
        VMresult.value = false
    }

    fun change() {
        VMresult.value = !VMresult.value!!
    }

}