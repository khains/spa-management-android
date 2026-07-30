package com.spa.management.ui.appointment

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.spa.management.data.model.Appointment
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import com.spa.management.ui.common.ErrorBanner
import com.spa.management.ui.common.appointmentStatusLabel
import com.spa.management.ui.common.formatDateTime
import com.spa.management.ui.customer.displayNameOf
import kotlinx.coroutines.launch

data class CheckInScanUiState(
    val loading: Boolean = false,
    val result: Appointment? = null,
    val error: String? = null
)

class CheckInScanViewModel : ViewModel() {
    var uiState by mutableStateOf(CheckInScanUiState())
        private set

    fun checkInByCode(code: String) {
        uiState = uiState.copy(loading = true, error = null, result = null)
        viewModelScope.launch {
            when (val res = SpaRepository.checkInByCode(code)) {
                is ApiResult.Success -> uiState = uiState.copy(loading = false, result = res.data)
                is ApiResult.Error -> uiState = uiState.copy(loading = false, error = res.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScanScreen(onBack: () -> Unit, vm: CheckInScanViewModel = viewModel()) {
    val state = vm.uiState
    var manualCode by remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            vm.checkInByCode(result.contents)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Check-in bằng mã") }, navigationIcon = {
                TextButton(onClick = onBack) { Text("Đóng") }
            })
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                val options = ScanOptions()
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                options.setPrompt("Quét mã QR check-in của khách")
                options.setBeepEnabled(true)
                scanLauncher.launch(options)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Mở camera quét mã QR")
            }

            Spacer(Modifier.height(16.dp))
            Text("Hoặc nhập mã check-in thủ công:")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = manualCode,
                onValueChange = { manualCode = it },
                label = { Text("Mã check-in") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.checkInByCode(manualCode.trim()) },
                enabled = manualCode.isNotBlank() && !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check-in")
            }

            Spacer(Modifier.height(24.dp))

            if (state.loading) {
                CircularProgressIndicator()
            }
            if (state.error != null) {
                ErrorBanner(state.error)
            }
            if (state.result != null) {
                val appt = state.result!!
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Check-in thành công!", style = MaterialTheme.typography.titleMedium)
                        Text("Khách: ${displayNameOf(appt.customer)}")
                        Text("Giờ hẹn: ${formatDateTime(appt.startTime)}")
                        Text("Trạng thái: ${appointmentStatusLabel(appt.status)}")
                    }
                }
            }
        }
    }
}
