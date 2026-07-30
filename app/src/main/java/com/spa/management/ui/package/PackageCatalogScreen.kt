package com.spa.management.ui.`package`

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spa.management.data.model.CreateServicePackageRequest
import com.spa.management.data.model.ServicePackage
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import com.spa.management.ui.common.ErrorBanner
import com.spa.management.ui.common.FullScreenLoading
import com.spa.management.ui.common.formatCurrency
import kotlinx.coroutines.launch

data class PackageCatalogUiState(
    val loading: Boolean = true,
    val packages: List<ServicePackage> = emptyList(),
    val error: String? = null,
    val showCreateDialog: Boolean = false
)

class PackageCatalogViewModel : ViewModel() {
    var uiState by mutableStateOf(PackageCatalogUiState())
        private set

    init { load() }

    fun load() {
        uiState = uiState.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val result = SpaRepository.getServicePackages()) {
                is ApiResult.Success -> uiState = uiState.copy(loading = false, packages = result.data)
                is ApiResult.Error -> uiState = uiState.copy(loading = false, error = result.message)
            }
        }
    }

    fun toggleCreateDialog(show: Boolean) {
        uiState = uiState.copy(showCreateDialog = show)
    }

    fun createPackage(name: String, description: String, sessions: Int, duration: Int, price: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            val result = SpaRepository.createServicePackage(
                CreateServicePackageRequest(name, description, emptyList(), sessions, duration, price)
            )
            if (result is ApiResult.Success) {
                toggleCreateDialog(false)
                load()
                onDone()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageCatalogScreen(vm: PackageCatalogViewModel = viewModel()) {
    val state = vm.uiState

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.toggleCreateDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Tạo gói mới")
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
                items(state.packages, key = { it.id }) { pkg -> ServicePackageCard(pkg) }
            }
        }

        if (state.showCreateDialog) {
            CreatePackageDialog(
                onDismiss = { vm.toggleCreateDialog(false) },
                onCreate = { name, desc, sessions, duration, price -> vm.createPackage(name, desc, sessions, duration, price) {} }
            )
        }
    }
}

@Composable
fun ServicePackageCard(pkg: ServicePackage) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(pkg.name, style = MaterialTheme.typography.titleMedium)
            if (!pkg.description.isNullOrBlank()) {
                Text(pkg.description, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
            Text("${pkg.totalSessions} buổi • Hạn sử dụng ${pkg.durationDays} ngày")
            Text(formatCurrency(pkg.price), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePackageDialog(onDismiss: () -> Unit, onCreate: (String, String, Int, Int, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sessions by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo gói liệu trình mới") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên gói") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = sessions, onValueChange = { sessions = it.filter(Char::isDigit) }, label = { Text("Số buổi") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = duration, onValueChange = { duration = it.filter(Char::isDigit) }, label = { Text("Thời hạn (ngày)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it.filter(Char::isDigit) }, label = { Text("Giá (VNĐ)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onCreate(name, description, sessions.toIntOrNull() ?: 0, duration.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0)
            }) { Text("Tạo") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
