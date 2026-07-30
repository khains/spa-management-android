package com.spa.management.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spa.management.data.model.CreateCustomerRequest
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import com.spa.management.ui.common.ErrorBanner
import kotlinx.coroutines.launch

data class CustomerFormState(
    val fullName: String = "",
    val phone: String = "",
    val dob: String = "", // dinh dang yyyy-MM-dd, nguoi dung nhap tay
    val address: String = "",
    val skinNotes: String = "",
    val source: String = "walk-in",
    val saving: Boolean = false,
    val error: String? = null
)

class CustomerFormViewModel : ViewModel() {
    var state by mutableStateOf(CustomerFormState())
        private set

    fun update(block: (CustomerFormState) -> CustomerFormState) {
        state = block(state)
    }

    fun save(onSuccess: () -> Unit) {
        if (state.fullName.isBlank() || state.phone.isBlank()) {
            state = state.copy(error = "Vui lòng nhập họ tên và số điện thoại")
            return
        }
        state = state.copy(saving = true, error = null)
        viewModelScope.launch {
            val result = SpaRepository.createCustomer(
                CreateCustomerRequest(
                    fullName = state.fullName.trim(),
                    phone = state.phone.trim(),
                    dob = state.dob.ifBlank { null },
                    address = state.address.ifBlank { null },
                    skinNotes = state.skinNotes.ifBlank { null },
                    source = state.source
                )
            )
            when (result) {
                is ApiResult.Success -> {
                    state = state.copy(saving = false)
                    onSuccess()
                }
                is ApiResult.Error -> state = state.copy(saving = false, error = result.message)
            }
        }
    }
}

val CUSTOMER_SOURCES = listOf("walk-in", "gioi_thieu", "facebook", "tiktok", "website", "khac")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(onSaved: () -> Unit, onBack: () -> Unit, vm: CustomerFormViewModel = viewModel()) {
    val s = vm.state
    var sourceExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm khách hàng") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Đóng") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = s.fullName,
                onValueChange = { vm.update { st -> st.copy(fullName = it) } },
                label = { Text("Họ và tên *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.phone,
                onValueChange = { vm.update { st -> st.copy(phone = it) } },
                label = { Text("Số điện thoại *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.dob,
                onValueChange = { vm.update { st -> st.copy(dob = it) } },
                label = { Text("Ngày sinh (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.address,
                onValueChange = { vm.update { st -> st.copy(address = it) } },
                label = { Text("Địa chỉ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.skinNotes,
                onValueChange = { vm.update { st -> st.copy(skinNotes = it) } },
                label = { Text("Ghi chú tình trạng da liễu / dị ứng") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            ExposedDropdownMenuBox(expanded = sourceExpanded, onExpandedChange = { sourceExpanded = it }) {
                OutlinedTextField(
                    value = s.source,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nguồn khách") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = sourceExpanded, onDismissRequest = { sourceExpanded = false }) {
                    CUSTOMER_SOURCES.forEach { src ->
                        DropdownMenuItem(text = { Text(src) }, onClick = {
                            vm.update { st -> st.copy(source = src) }
                            sourceExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            if (s.error != null) {
                ErrorBanner(s.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { vm.save(onSaved) },
                enabled = !s.saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (s.saving) "Đang lưu..." else "Lưu khách hàng")
            }
        }
    }
}
