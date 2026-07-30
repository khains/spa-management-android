package com.spa.management.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spa.management.data.model.Customer
import com.spa.management.ui.common.ErrorBanner
import com.spa.management.ui.common.FullScreenLoading
import com.spa.management.ui.common.tagStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onCustomerClick: (String) -> Unit,
    onAddCustomer: () -> Unit,
    vm: CustomerListViewModel = viewModel()
) {
    val state = vm.uiState

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCustomer) {
                Icon(Icons.Default.Add, contentDescription = "Thêm khách hàng")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.search,
                onValueChange = vm::onSearchChange,
                label = { Text("Tìm theo tên hoặc SĐT") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                items(FILTER_TAGS) { (tag, label) ->
                    FilterChip(
                        selected = state.selectedTag == tag,
                        onClick = { vm.onTagSelect(tag) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                state.loading -> FullScreenLoading()
                state.error != null -> ErrorBanner(state.error)
                state.customers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có khách hàng nào phù hợp")
                }
                else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.customers, key = { it.id }) { customer ->
                        CustomerCard(customer, onClick = { onCustomerClick(customer.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(customer: Customer, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(customer.fullName, style = MaterialTheme.typography.titleMedium)
            Text(customer.phone, style = MaterialTheme.typography.bodyMedium)
            if (customer.tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(customer.tags) { tag ->
                        val style = tagStyle(tag)
                        AssistChip(onClick = {}, label = { Text(style.label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }
        }
    }
}
