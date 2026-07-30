package com.spa.management.ui.appointment

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
import com.spa.management.data.model.CreateAppointmentRequest
import com.spa.management.data.model.CustomerPackage
import com.spa.management.data.model.Staff
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import com.spa.management.ui.common.ErrorBanner
import com.spa.management.ui.common.SimpleViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BookAppointmentUiState(
    val loadingOptions: Boolean = true,
    val technicians: List<Staff> = emptyList(),
    val customerPackages: List<CustomerPackage> = emptyList(),
    val selectedTechnicianId: String? = null,
    val selectedPackageId: String? = null,
    val serviceName: String = "",
    val room: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    val time: String = "09:00",
    val durationMinutes: String = "60",
    val note: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

// customerId co the null neu dat lich tu man hinh chung (chua co san khach)
class BookAppointmentViewModel(private val customerId: String?) : ViewModel() {
    var uiState by mutableStateOf(BookAppointmentUiState())
        private set

    init { loadOptions() }

    fun loadOptions() {
        viewModelScope.launch {
            val staffResult = SpaRepository.getStaffList()
            val pkgResult = if (customerId != null) {
                SpaRepository.getCustomerPackages(status = "active", customer = customerId)
            } else null

            val technicians = (staffResult as? ApiResult.Success)?.data?.filter { it.role == "technician" } ?: emptyList()
            val packages = (pkgResult as? ApiResult.Success)?.data ?: emptyList()

            uiState = uiState.copy(loadingOptions = false, technicians = technicians, customerPackages = packages)
        }
    }

    fun update(block: (BookAppointmentUiState) -> BookAppointmentUiState) {
        uiState = block(uiState)
    }

    fun book(customerIdOverride: String?) {
        val finalCustomerId = customerId ?: customerIdOverride
        if (finalCustomerId.isNullOrBlank()) {
            uiState = uiState.copy(error = "Thiếu thông tin khách hàng")
            return
        }
        uiState = uiState.copy(saving = true, error = null)
        val startTimeIso = "${uiState.date}T${uiState.time}:00.000Z"
        viewModelScope.launch {
            val result = SpaRepository.createAppointment(
                CreateAppointmentRequest(
                    customerId = finalCustomerId,
                    customerPackageId = uiState.selectedPackageId,
                    technicianId = uiState.selectedTechnicianId,
                    room = uiState.room.ifBlank { null },
                    serviceName = uiState.serviceName.ifBlank { null },
                    startTime = startTimeIso,
                    durationMinutes = uiState.durationMinutes.toIntOrNull() ?: 60,
                    note = uiState.note.ifBlank { null }
                )
            )
            when (result) {
                is ApiResult.Success -> uiState = uiState.copy(saving = false, done = true)
                is ApiResult.Error -> uiState = uiState.copy(saving = false, error = result.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    customerId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    vm: BookAppointmentViewModel = viewModel(factory = SimpleViewModelFactory { BookAppointmentViewModel(customerId) })
) {
    val s = vm.uiState
    var techExpanded by remember { mutableStateOf(false) }
    var pkgExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(s.done) { if (s.done) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Đặt lịch hẹn") }, navigationIcon = {
                TextButton(onClick = onBack) { Text("Đóng") }
            })
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()).fillMaxSize()
        ) {
            if (s.customerPackages.isNotEmpty()) {
                ExposedDropdownMenuBox(expanded = pkgExpanded, onExpandedChange = { pkgExpanded = it }) {
                    val selectedName = s.customerPackages.find { it.id == s.selectedPackageId }?.packageNameSnapshot ?: "Không trừ buổi (buổi lẻ)"
                    OutlinedTextField(
                        value = selectedName, onValueChange = {}, readOnly = true,
                        label = { Text("Gói liệu trình áp dụng") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = pkgExpanded, onDismissRequest = { pkgExpanded = false }) {
                        DropdownMenuItem(text = { Text("Không trừ buổi (buổi lẻ)") }, onClick = {
                            vm.update { it.copy(selectedPackageId = null) }; pkgExpanded = false
                        })
                        s.customerPackages.forEach { pkg ->
                            DropdownMenuItem(text = { Text("${pkg.packageNameSnapshot} (còn ${pkg.sessionsRemaining ?: (pkg.sessionsTotal - pkg.sessionsUsed)} buổi)") }, onClick = {
                                vm.update { it.copy(selectedPackageId = pkg.id) }; pkgExpanded = false
                            })
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            ExposedDropdownMenuBox(expanded = techExpanded, onExpandedChange = { techExpanded = it }) {
                val selectedTechName = s.technicians.find { it.id == s.selectedTechnicianId }?.fullName ?: "Chưa chọn"
                OutlinedTextField(
                    value = selectedTechName, onValueChange = {}, readOnly = true,
                    label = { Text("Kỹ thuật viên") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = techExpanded, onDismissRequest = { techExpanded = false }) {
                    s.technicians.forEach { tech ->
                        DropdownMenuItem(text = { Text(tech.fullName) }, onClick = {
                            vm.update { it.copy(selectedTechnicianId = tech.id) }; techExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.serviceName, onValueChange = { v -> vm.update { it.copy(serviceName = v) } },
                label = { Text("Tên dịch vụ") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.room, onValueChange = { v -> vm.update { it.copy(room = v) } },
                label = { Text("Phòng") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row {
                OutlinedTextField(
                    value = s.date, onValueChange = { v -> vm.update { it.copy(date = v) } },
                    label = { Text("Ngày (yyyy-MM-dd)") }, singleLine = true, modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = s.time, onValueChange = { v -> vm.update { it.copy(time = v) } },
                    label = { Text("Giờ (HH:mm)") }, singleLine = true, modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.durationMinutes, onValueChange = { v -> vm.update { it.copy(durationMinutes = v.filter(Char::isDigit)) } },
                label = { Text("Thời lượng (phút)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.note, onValueChange = { v -> vm.update { it.copy(note = v) } },
                label = { Text("Ghi chú") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            if (s.error != null) {
                ErrorBanner(s.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(onClick = { vm.book(customerId) }, enabled = !s.saving, modifier = Modifier.fillMaxWidth()) {
                Text(if (s.saving) "Đang đặt lịch..." else "Đặt lịch hẹn")
            }
        }
    }
}
