package com.spa.management.ui.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spa.management.data.model.Appointment
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import com.spa.management.ui.common.*
import com.spa.management.ui.customer.displayNameOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AppointmentListUiState(
    val loading: Boolean = true,
    val dateIso: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    val appointments: List<Appointment> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

class AppointmentListViewModel : ViewModel() {
    var uiState by mutableStateOf(AppointmentListUiState())
        private set

    init { load() }

    fun changeDate(days: Int) {
        val cal = Calendar.getInstance()
        cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(uiState.dateIso) ?: Date()
        cal.add(Calendar.DAY_OF_MONTH, days)
        uiState = uiState.copy(dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time))
        load()
    }

    fun load() {
        uiState = uiState.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val result = SpaRepository.getAppointments(date = uiState.dateIso)) {
                is ApiResult.Success -> uiState = uiState.copy(loading = false, appointments = result.data)
                is ApiResult.Error -> uiState = uiState.copy(loading = false, error = result.message)
            }
        }
    }

    fun checkIn(appointmentId: String) {
        viewModelScope.launch {
            when (val result = SpaRepository.checkInById(appointmentId)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(message = "Check-in thành công, đã trừ buổi")
                    load()
                }
                is ApiResult.Error -> uiState = uiState.copy(message = result.message)
            }
        }
    }

    fun clearMessage() { uiState = uiState.copy(message = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(
    onBookAppointment: () -> Unit,
    onScanCheckIn: () -> Unit,
    vm: AppointmentListViewModel = viewModel()
) {
    val state = vm.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(onClick = onScanCheckIn) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Quét mã check-in")
                }
                Spacer(Modifier.height(8.dp))
                FloatingActionButton(onClick = onBookAppointment) {
                    Icon(Icons.Default.Add, contentDescription = "Đặt lịch hẹn")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { vm.changeDate(-1) }) { Text("‹ Hôm trước") }
                Text(state.dateIso, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { vm.changeDate(1) }) { Text("Hôm sau ›") }
            }

            when {
                state.loading -> FullScreenLoading()
                state.error != null -> ErrorBanner(state.error)
                state.appointments.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có lịch hẹn trong ngày này")
                }
                else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.appointments, key = { it.id }) { appt ->
                        AppointmentRow(appt, onCheckIn = { vm.checkIn(appt.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentRow(appt: Appointment, onCheckIn: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(formatDateTime(appt.startTime), style = MaterialTheme.typography.titleMedium)
                AssistChip(onClick = {}, label = { Text(appointmentStatusLabel(appt.status)) })
            }
            Text("Khách: ${displayNameOf(appt.customer)}")
            val tech = displayNameOf(appt.technician)
            if (tech != "--") Text("Kỹ thuật viên: $tech")
            if (!appt.serviceName.isNullOrBlank()) Text("Dịch vụ: ${appt.serviceName}")
            if (!appt.room.isNullOrBlank()) Text("Phòng: ${appt.room}")

            if (appt.status == "booked") {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onCheckIn) { Text("Check-in (trừ 1 buổi)") }
            }
        }
    }
}
