package com.spa.management.ui.`package`

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spa.management.data.model.ServicePackage
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import com.spa.management.ui.common.ErrorBanner
import com.spa.management.ui.common.FullScreenLoading
import com.spa.management.ui.common.SimpleViewModelFactory
import com.spa.management.ui.common.formatCurrency
import kotlinx.coroutines.launch

data class AssignPackageUiState(
    val loading: Boolean = true,
    val packages: List<ServicePackage> = emptyList(),
    val error: String? = null,
    val assigning: Boolean = false,
    val done: Boolean = false
)

class AssignPackageViewModel(private val customerId: String) : ViewModel() {
    var uiState by mutableStateOf(AssignPackageUiState())
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            when (val result = SpaRepository.getServicePackages()) {
                is ApiResult.Success -> uiState = uiState.copy(loading = false, packages = result.data)
                is ApiResult.Error -> uiState = uiState.copy(loading = false, error = result.message)
            }
        }
    }

    fun assign(servicePackageId: String) {
        uiState = uiState.copy(assigning = true)
        viewModelScope.launch {
            when (val result = SpaRepository.assignPackage(customerId, servicePackageId)) {
                is ApiResult.Success -> uiState = uiState.copy(assigning = false, done = true)
                is ApiResult.Error -> uiState = uiState.copy(assigning = false, error = result.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignPackageScreen(
    customerId: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    vm: AssignPackageViewModel = viewModel(factory = SimpleViewModelFactory { AssignPackageViewModel(customerId) })
) {
    val state = vm.uiState

    LaunchedEffect(state.done) {
        if (state.done) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gán gói liệu trình") }, navigationIcon = {
                TextButton(onClick = onBack) { Text("Đóng") }
            })
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
                items(state.packages, key = { it.id }) { pkg ->
                    Card(
                        onClick = { if (!state.assigning) vm.assign(pkg.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(pkg.name, style = MaterialTheme.typography.titleMedium)
                            Text("${pkg.totalSessions} buổi • ${pkg.durationDays} ngày")
                            Text(formatCurrency(pkg.price))
                        }
                    }
                }
            }
        }
    }
}
