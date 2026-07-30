package com.spa.management.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Factory don gian de tao ViewModel co tham so constructor (khong dung Hilt/Dagger)
class SimpleViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
