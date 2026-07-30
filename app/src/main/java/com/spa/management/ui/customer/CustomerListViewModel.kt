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

data class CustomerListUiState(
    val loading: Boolean = true,
    val customers: List<Customer> = emptyList(),
    val search: String = "",
    val selectedTag: String? = null,
    val error: String? = null
)

// Cac nhan phan loai de hien thi bo loc nhanh
val FILTER_TAGS = listOf(
    "moi" to "Mới",
    "dang_dung_lieu_trinh" to "Đang dùng liệu trình",
    "vip" to "VIP",
    "sap_het_buoi" to "Sắp hết buổi",
    "sap_het_han" to "Sắp hết hạn"
)

class CustomerListViewModel : ViewModel() {
    var uiState by mutableStateOf(CustomerListUiState())
        private set

    init {
        load()
    }

    fun onSearchChange(v: String) {
        uiState = uiState.copy(search = v)
        load()
    }

    fun onTagSelect(tag: String?) {
        uiState = uiState.copy(selectedTag = if (uiState.selectedTag == tag) null else tag)
        load()
    }

    fun load() {
        uiState = uiState.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val result = SpaRepository.getCustomers(
                search = uiState.search.ifBlank { null },
                tag = uiState.selectedTag
            )) {
                is ApiResult.Success -> uiState = uiState.copy(loading = false, customers = result.data)
                is ApiResult.Error -> uiState = uiState.copy(loading = false, error = result.message)
            }
        }
    }
}
