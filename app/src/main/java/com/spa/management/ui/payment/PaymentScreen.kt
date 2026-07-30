package com.spa.management.ui.payment

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
import com.spa.management.data.model.CreatePaymentRequest
import com.spa.management.data.model.CustomerPackage
import com.spa.management.data.model.InstallmentInfo
import com.spa.management.data.repository.ApiResult
import com.spa.management.data.repository.SpaRepository
import com.spa.management.ui.common.ErrorBanner
import com.spa.management.ui.common.SimpleViewModelFactory
import com.spa.management.ui.common.paymentMethodLabel
import kotlinx.coroutines.launch

val PAYMENT_METHODS = listOf("tien_mat", "chuyen_khoan", "tra_gop")

data class PaymentUiState(
    val loadingOptions: Boolean = true,
    val customerPackages: List<CustomerPackage> = emptyList(),
    val selectedPackageId: String? = null,
    val amount: String = "",
    val method: String = "tien_mat",
    val note: String = "",
    val installmentNumber: String = "1",
    val totalInstallments: String = "1",
    val saving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

class PaymentViewModel(private val customerId: String) : ViewModel() {
    var uiState by mutableStateOf(PaymentUiState())
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            when (val result = SpaRepository.getCustomerPackages(customer = customerId)) {
                is ApiResult.Success -> uiState = uiState.copy(loadingOptions = false, customerPackages = result.data)
                is ApiResult.Error -> uiState = uiState.copy(loadingOptions = false, error = result.message)
            }
        }
    }

    fun update(block: (PaymentUiState) -> PaymentUiState) {
        uiState = block(uiState)
    }

    fun submit() {
        val amountValue = uiState.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            uiState = uiState.copy(error = "Vui lòng nhập số tiền hợp lệ")
            return
        }
        uiState = uiState.copy(saving = true, error = null)
        viewModelScope.launch {
            val installment = if (uiState.method == "tra_gop") {
                InstallmentInfo(
                    totalAmount = amountValue,
                    installmentNumber = uiState.installmentNumber.toIntOrNull() ?: 1,
                    totalInstallments = uiState.totalInstallments.toIntOrNull() ?: 1
                )
            } else null

            val result = SpaRepository.createPayment(
                CreatePaymentRequest(
                    customerId = customerId,
                    customerPackageId = uiState.selectedPackageId,
                    amount = amountValue,
                    method = uiState.method,
                    note = uiState.note.ifBlank { null },
                    installment = installment
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
fun PaymentScreen(
    customerId: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    vm: PaymentViewModel = viewModel(factory = SimpleViewModelFactory { PaymentViewModel(customerId) })
) {
    val s = vm.uiState
    var pkgExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(s.done) { if (s.done) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ghi nhận thanh toán") }, navigationIcon = {
                TextButton(onClick = onBack) { Text("Đóng") }
            })
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()).fillMaxSize()
        ) {
            if (!s.loadingOptions && s.customerPackages.isNotEmpty()) {
                ExposedDropdownMenuBox(expanded = pkgExpanded, onExpandedChange = { pkgExpanded = it }) {
                    val selectedName = s.customerPackages.find { it.id == s.selectedPackageId }?.packageNameSnapshot ?: "Không gắn với gói cụ thể"
                    OutlinedTextField(
                        value = selectedName, onValueChange = {}, readOnly = true,
                        label = { Text("Áp dụng cho gói") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = pkgExpanded, onDismissRequest = { pkgExpanded = false }) {
                        DropdownMenuItem(text = { Text("Không gắn với gói cụ thể") }, onClick = {
                            vm.update { it.copy(selectedPackageId = null) }; pkgExpanded = false
                        })
                        s.customerPackages.forEach { pkg ->
                            DropdownMenuItem(text = { Text(pkg.packageNameSnapshot) }, onClick = {
                                vm.update { it.copy(selectedPackageId = pkg.id) }; pkgExpanded = false
                            })
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = s.amount,
                onValueChange = { v -> vm.update { it.copy(amount = v.filter(Char::isDigit)) } },
                label = { Text("Số tiền (VNĐ)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            ExposedDropdownMenuBox(expanded = methodExpanded, onExpandedChange = { methodExpanded = it }) {
                OutlinedTextField(
                    value = paymentMethodLabel(s.method), onValueChange = {}, readOnly = true,
                    label = { Text("Hình thức thanh toán") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                    PAYMENT_METHODS.forEach { m ->
                        DropdownMenuItem(text = { Text(paymentMethodLabel(m)) }, onClick = {
                            vm.update { it.copy(method = m) }; methodExpanded = false
                        })
                    }
                }
            }

            if (s.method == "tra_gop") {
                Spacer(Modifier.height(10.dp))
                Row {
                    OutlinedTextField(
                        value = s.installmentNumber,
                        onValueChange = { v -> vm.update { it.copy(installmentNumber = v.filter(Char::isDigit)) } },
                        label = { Text("Lần trả thứ") }, singleLine = true, modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = s.totalInstallments,
                        onValueChange = { v -> vm.update { it.copy(totalInstallments = v.filter(Char::isDigit)) } },
                        label = { Text("Tổng số lần trả") }, singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = s.note,
                onValueChange = { v -> vm.update { it.copy(note = v) } },
                label = { Text("Ghi chú") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            if (s.error != null) {
                ErrorBanner(s.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(onClick = { vm.submit() }, enabled = !s.saving, modifier = Modifier.fillMaxWidth()) {
                Text(if (s.saving) "Đang lưu..." else "Ghi nhận thanh toán")
            }
        }
    }
}
