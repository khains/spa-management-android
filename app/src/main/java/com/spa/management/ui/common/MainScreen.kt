package com.spa.management.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spa.management.data.api.SessionManager
import com.spa.management.ui.appointment.AppointmentListScreen
import com.spa.management.ui.customer.CustomerListScreen
import com.spa.management.ui.`package`.PackageCatalogScreen
import com.spa.management.ui.staff.StaffManagementScreen

// 3-4 tab chinh: Khach hang, Goi lieu trinh, Lich hen, va Nhan vien (chi admin).
// Cac man hinh chi tiet/tao moi duoc dieu huong o cap NavGraph ben ngoai (root nav controller).
@Composable
fun MainScreen(
    rootNavController: NavHostController
) {
    val bottomNavController = rememberNavController()
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAdmin = SessionManager.getStaffRole() == "admin"
    }

    Scaffold(
        bottomBar = { MainBottomBar(bottomNavController, isAdmin) }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "tab_customers",
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable("tab_customers") {
                CustomerListScreen(
                    onCustomerClick = { id -> rootNavController.navigate("customer_detail/$id") },
                    onAddCustomer = { rootNavController.navigate("customer_form") }
                )
            }
            composable("tab_packages") {
                PackageCatalogScreen()
            }
            composable("tab_appointments") {
                AppointmentListScreen(
                    onBookAppointment = { rootNavController.navigate("book_appointment") },
                    onScanCheckIn = { rootNavController.navigate("check_in_scan") }
                )
            }
            composable("tab_staff") {
                // De phong truong hop token het han giua chung hoac khong phai admin,
                // backend van se tu chan (403) neu goi API tao nhan vien.
                StaffManagementScreen(onBack = { bottomNavController.popBackStack("tab_customers", false) })
            }
        }
    }
}

@Composable
fun MainBottomBar(navController: NavHostController, isAdmin: Boolean) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "tab_customers",
            onClick = { navController.navigate("tab_customers") { launchSingleTop = true } },
            icon = { Icon(Icons.Default.People, contentDescription = null) },
            label = { Text("Khách hàng") }
        )
        NavigationBarItem(
            selected = currentRoute == "tab_packages",
            onClick = { navController.navigate("tab_packages") { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
            label = { Text("Gói liệu trình") }
        )
        NavigationBarItem(
            selected = currentRoute == "tab_appointments",
            onClick = { navController.navigate("tab_appointments") { launchSingleTop = true } },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            label = { Text("Lịch hẹn") }
        )
        if (isAdmin) {
            NavigationBarItem(
                selected = currentRoute == "tab_staff",
                onClick = { navController.navigate("tab_staff") { launchSingleTop = true } },
                icon = { Icon(Icons.Default.Badge, contentDescription = null) },
                label = { Text("Nhân viên") }
            )
        }
    }
}
