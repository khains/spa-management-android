package com.spa.management.ui.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.management.data.model.Customer
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import kotlinx.coroutines.launch

data class CustomerDetailUiState(
    val loading: Boolean = true,
    val customer: Customer? = null,
    val error: String? = null,
    val newNote: String = "",
    val savingNote: Boolean = false
)

class CustomerDetailViewModel(private val customerId: String) : ViewModel() {
    var uiState by mutableStateOf(CustomerDetailUiState())
        private set

    init {
        load()
    }

    fun load() {
        uiState = uiState.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val result = SpaRepository.getCustomerDetail(customerId)) {
                is ApiResult.Success -> uiState = uiState.copy(loading = false, customer = result.data)
                is ApiResult.Error -> uiState = uiState.copy(loading = false, error = result.message)
            }
        }
    }

    fun onNoteChange(v: String) {
        uiState = uiState.copy(newNote = v)
    }

    fun addNote() {
        if (uiState.newNote.isBlank()) return
        uiState = uiState.copy(savingNote = true)
        viewModelScope.launch {
            when (SpaRepository.addCustomerNote(customerId, uiState.newNote.trim())) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(savingNote = false, newNote = "")
                    load()
                }
                is ApiResult.Error -> uiState = uiState.copy(savingNote = false)
            }
        }
    }
}
