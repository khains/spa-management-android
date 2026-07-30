package com.spa.management.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spa.management.data.model.CreateStaffRequest
import com.spa.management.data.model.Staff
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import com.spa.management.ui.common.ErrorBanner
import com.spa.management.ui.common.FullScreenLoading
import kotlinx.coroutines.launch

// Nhan hien thi cho tung vai tro
fun roleLabel(role: String): String = when (role) {
    "admin" -> "Quản trị viên"
    "receptionist" -> "Lễ tân"
    "technician" -> "Kỹ thuật viên"
    else -> role
}

val STAFF_ROLES = listOf("technician", "receptionist", "admin")

data class StaffManagementUiState(
    val loading: Boolean = true,
    val staffList: List<Staff> = emptyList(),
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val creating: Boolean = false,
    val createError: String? = null
)

class StaffManagementViewModel : ViewModel() {
    var uiState by mutableStateOf(StaffManagementUiState())
        private set

    init { load() }

    fun load() {
        uiState = uiState.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val result = SpaRepository.getStaffList()) {
                is ApiResult.Success -> uiState = uiState.copy(loading = false, staffList = result.data)
                is ApiResult.Error -> uiState = uiState.copy(loading = false, error = result.message)
            }
        }
    }

    fun toggleCreateDialog(show: Boolean) {
        uiState = uiState.copy(showCreateDialog = show, createError = null)
    }

    fun createStaff(
        fullName: String,
        username: String,
        password: String,
        role: String,
        phone: String,
        workingHours: String
    ) {
        if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
            uiState = uiState.copy(createError = "Vui lòng nhập đủ họ tên, tên đăng nhập và mật khẩu")
            return
        }
        uiState = uiState.copy(creating = true, createError = null)
        viewModelScope.launch {
            val result = SpaRepository.createStaff(
                CreateStaffRequest(
                    fullName = fullName.trim(),
                    username = username.trim(),
                    password = password,
                    role = role,
                    phone = phone.ifBlank { null },
                    workingHours = workingHours.ifBlank { null }
                )
            )
            when (result) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(creating = false, showCreateDialog = false)
                    load()
                }
                is ApiResult.Error -> uiState = uiState.copy(creating = false, createError = result.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(onBack: () -> Unit, vm: StaffManagementViewModel = viewModel()) {
    val state = vm.uiState

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Quản lý nhân viên") }, navigationIcon = {
                TextButton(onClick = onBack) { Text("Quay lại") }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.toggleCreateDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Tạo tài khoản nhân viên")
            }
        }
    ) { padding ->
        when {
            state.loading -> Box(Modifier.padding(padding).fillMaxSize()) { FullScreenLoading() }
            state.error != null -> Box(Modifier.padding(padding)) { ErrorBanner(state.error) }
            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.staffList, key = { it.id }) { staff -> StaffCard(staff) }
            }
        }

        if (state.showCreateDialog) {
            CreateStaffDialog(
                creating = state.creating,
                error = state.createError,
                onDismiss = { vm.toggleCreateDialog(false) },
                onCreate = { fullName, username, password, role, phone, workingHours ->
                    vm.createStaff(fullName, username, password, role, phone, workingHours)
                }
            )
        }
    }
}

@Composable
fun StaffCard(staff: Staff) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(staff.fullName, style = MaterialTheme.typography.titleMedium)
            Text("Tên đăng nhập: ${staff.username}", style = MaterialTheme.typography.bodyMedium)
            Text("Vai trò: ${roleLabel(staff.role)}", style = MaterialTheme.typography.bodySmall)
            if (!staff.phone.isNullOrBlank()) {
                Text("SĐT: ${staff.phone}", style = MaterialTheme.typography.bodySmall)
            }
            if (!staff.workingHours.isNullOrBlank()) {
                Text("Giờ làm việc: ${staff.workingHours}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStaffDialog(
    creating: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (fullName: String, username: String, password: String, role: String, phone: String, workingHours: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("technician") }
    var phone by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("08:00-20:00") }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo tài khoản nhân viên") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = fullName, onValueChange = { fullName = it },
                    label = { Text("Họ và tên") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Tên đăng nhập") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Mật khẩu") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(
                        value = roleLabel(role), onValueChange = {}, readOnly = true,
                        label = { Text("Vai trò") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        STAFF_ROLES.forEach { r ->
                            DropdownMenuItem(text = { Text(roleLabel(r)) }, onClick = {
                                role = r; roleExpanded = false
                            })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Số điện thoại") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )

                if (role == "technician") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = workingHours, onValueChange = { workingHours = it },
                        label = { Text("Giờ làm việc (vd 08:00-20:00)") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    ErrorBanner(error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(fullName, username, password, role, phone, workingHours) },
                enabled = !creating
            ) { Text(if (creating) "Đang tạo..." else "Tạo tài khoản") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !creating) { Text("Hủy") } }
    )
}
