package com.spa.management.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spa.management.ui.appointment.BookAppointmentScreen
import com.spa.management.ui.appointment.CheckInScanScreen
import com.spa.management.ui.auth.LoginScreen
import com.spa.management.ui.common.MainScreen
import com.spa.management.ui.customer.CustomerDetailScreen
import com.spa.management.ui.customer.CustomerFormScreen
import com.spa.management.ui.`package`.AssignPackageScreen
import com.spa.management.ui.payment.PaymentScreen

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable("main") {
            MainScreen(rootNavController = navController)
        }

        composable("customer_form") {
            CustomerFormScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "customer_detail/{customerId}",
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            CustomerDetailScreen(
                customerId = customerId,
                onBack = { navController.popBackStack() },
                onAssignPackage = { id -> navController.navigate("assign_package/$id") },
                onBookAppointment = { id -> navController.navigate("book_appointment?customerId=$id") },
                onRecordPayment = { id -> navController.navigate("payment/$id") }
            )
        }

        composable(
            route = "assign_package/{customerId}",
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            AssignPackageScreen(
                customerId = customerId,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "book_appointment?customerId={customerId}",
            arguments = listOf(navArgument("customerId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            BookAppointmentScreen(
                customerId = customerId,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("check_in_scan") {
            CheckInScanScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "payment/{customerId}",
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            PaymentScreen(
                customerId = customerId,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
