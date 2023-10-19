package com.lahsuak.apps.tasks.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class MainViewModel : ViewModel() {
    private val _isScreenLoaded = MutableStateFlow<Boolean>(false)
    val isScreenLoaded = _isScreenLoaded.asStateFlow()

    fun update(loaded: Boolean){
        _isScreenLoaded.value = loaded
    }
}