package com.spa.management.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.internal.LinkedTreeMap
import com.spa.management.data.model.Appointment
import com.spa.management.data.model.CustomerPackage
import com.spa.management.ui.common.*

// Cac model tra ve tu API co truong "customer"/"servicePackage" co the la String hoac object populate.
// Ham nay lay ten hien thi mot cach an toan.
@Suppress("UNCHECKED_CAST")
fun displayNameOf(field: Any?): String {
    return when (field) {
        is String -> field
        is Map<*, *> -> (field["fullName"] ?: field["name"] ?: "").toString()
        is LinkedTreeMap<*, *> -> (field["fullName"] ?: field["name"] ?: "").toString()
        else -> "--"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String,
    onBack: () -> Unit,
    onAssignPackage: (String) -> Unit,
    onBookAppointment: (String) -> Unit,
    onRecordPayment: (String) -> Unit,
    vm: CustomerDetailViewModel = viewModel(factory = SimpleViewModelFactory { CustomerDetailViewModel(customerId) })
) {
    val state = vm.uiState

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(state.customer?.fullName ?: "Chi tiết khách hàng") }, navigationIcon = {
                TextButton(onClick = onBack) { Text("Quay lại") }
            })
        }
    ) { padding ->
        when {
            state.loading -> Box(Modifier.padding(padding).fillMaxSize()) { FullScreenLoading() }
            state.error != null -> Box(Modifier.padding(padding)) { ErrorBanner(state.error) }
            state.customer != null -> {
                val c = state.customer
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column {
                            Text(c.phone, style = MaterialTheme.typography.bodyMedium)
                            if (!c.skinNotes.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text("Ghi chú da liễu: ${c.skinNotes}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (c.tags.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(c.tags) { tag ->
                                        AssistChip(onClick = {}, label = { Text(tagStyle(tag).label) })
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onAssignPackage(customerId) }) { Text("Gán gói") }
                            OutlinedButton(onClick = { onBookAppointment(customerId) }) { Text("Đặt lịch") }
                            OutlinedButton(onClick = { onRecordPayment(customerId) }) { Text("Thanh toán") }
                        }
                    }

                    item {
                        Text("Gói liệu trình", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    val packages = c.packages ?: emptyList()
                    if (packages.isEmpty()) {
                        item { Text("Chưa có gói liệu trình nào", style = MaterialTheme.typography.bodySmall) }
                    } else {
                        items(packages) { pkg -> PackageProgressCard(pkg) }
                    }

                    item {
                        Text("Lịch sử buổi hẹn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    val appts = c.appointments ?: emptyList()
                    if (appts.isEmpty()) {
                        item { Text("Chưa có lịch hẹn nào", style = MaterialTheme.typography.bodySmall) }
                    } else {
                        items(appts) { appt -> AppointmentHistoryRow(appt) }
                    }

                    item {
                        Text("Ghi chú nội bộ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(c.internalNotes) { note ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(note.content, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${note.staff?.fullName ?: ""} • ${formatDateTime(note.date)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = state.newNote,
                                onValueChange = vm::onNoteChange,
                                label = { Text("Thêm ghi chú mới") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { vm.addNote() }, enabled = !state.savingNote) { Text("Lưu") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackageProgressCard(pkg: CustomerPackage) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(pkg.packageNameSnapshot, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            val remaining = pkg.sessionsRemaining ?: (pkg.sessionsTotal - pkg.sessionsUsed)
            Text("Đã dùng ${pkg.sessionsUsed}/${pkg.sessionsTotal} buổi (còn $remaining)")
            LinearProgressIndicator(
                progress = { if (pkg.sessionsTotal > 0) pkg.sessionsUsed.toFloat() / pkg.sessionsTotal else 0f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            )
            Text("Từ ${formatDate(pkg.startDate)} đến ${formatDate(pkg.endDate)}", style = MaterialTheme.typography.bodySmall)
            Text("Trạng thái: ${packageStatusLabel(pkg.status)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AppointmentHistoryRow(appt: Appointment) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text("${formatDateTime(appt.startTime)} • ${appointmentStatusLabel(appt.status)}", style = MaterialTheme.typography.bodyMedium)
        val techName = displayNameOf(appt.technician)
        if (techName != "--") Text("Kỹ thuật viên: $techName", style = MaterialTheme.typography.bodySmall)
        if (!appt.resultNote.isNullOrBlank()) {
            Text("Kết quả: ${appt.resultNote}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
