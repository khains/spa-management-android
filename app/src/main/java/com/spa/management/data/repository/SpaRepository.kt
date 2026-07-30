package com.spa.management.data.repository

import com.google.gson.Gson
import com.spa.management.data.api.RetrofitClient
import com.spa.management.data.model.*
import retrofit2.Response

// Ket qua chung: thanh cong voi data, hoac loi voi thong diep tieng Viet tu server
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

object SpaRepository {
    private val api = RetrofitClient.api
    private val gson = Gson()

    private fun <T> Response<T>.toResult(): ApiResult<T> {
        return if (isSuccessful && body() != null) {
            ApiResult.Success(body()!!)
        } else {
            val errBody = errorBody()?.string()
            val msg = try {
                gson.fromJson(errBody, ApiMessage::class.java)?.message
            } catch (e: Exception) {
                null
            } ?: "Da xay ra loi (${code()})"
            ApiResult.Error(msg)
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): ApiResult<T> {
        return try {
            block().toResult()
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Khong the ket noi toi may chu")
        }
    }

    // ----- Auth -----
    suspend fun login(username: String, password: String) =
        safeCall { api.login(LoginRequest(username, password)) }

    suspend fun getStaffList() = safeCall { api.getStaffList() }

    suspend fun createStaff(req: CreateStaffRequest) = safeCall { api.createStaff(req) }

    // ----- Customers -----
    suspend fun getCustomers(search: String? = null, tag: String? = null) =
        safeCall { api.getCustomers(search, tag) }

    suspend fun getCustomerDetail(id: String) = safeCall { api.getCustomerDetail(id) }

    suspend fun createCustomer(req: CreateCustomerRequest) = safeCall { api.createCustomer(req) }

    suspend fun updateCustomer(id: String, fields: Map<String, Any?>) =
        safeCall { api.updateCustomer(id, fields) }

    suspend fun deleteCustomer(id: String) = safeCall { api.deleteCustomer(id) }

    suspend fun addCustomerNote(id: String, content: String) =
        safeCall { api.addCustomerNote(id, AddNoteRequest(content)) }

    // ----- Service packages -----
    suspend fun getServicePackages() = safeCall { api.getServicePackages() }

    suspend fun createServicePackage(req: CreateServicePackageRequest) =
        safeCall { api.createServicePackage(req) }

    suspend fun deleteServicePackage(id: String) = safeCall { api.deleteServicePackage(id) }

    // ----- Customer packages -----
    suspend fun getCustomerPackages(
        status: String? = null,
        customer: String? = null,
        lowSessions: Boolean? = null,
        expiringSoon: Boolean? = null
    ) = safeCall { api.getCustomerPackages(status, customer, lowSessions, expiringSoon) }

    suspend fun assignPackage(customerId: String, servicePackageId: String, startDate: String? = null) =
        safeCall { api.assignPackage(AssignPackageRequest(customerId, servicePackageId, startDate)) }

    suspend fun renewPackage(id: String, startDate: String? = null, sessionsTotal: Int? = null, durationDays: Int? = null) =
        safeCall { api.renewPackage(id, RenewPackageRequest(startDate, sessionsTotal, durationDays)) }

    // ----- Appointments -----
    suspend fun getAppointments(date: String? = null, technician: String? = null, status: String? = null, customer: String? = null) =
        safeCall { api.getAppointments(date, technician, status, customer) }

    suspend fun createAppointment(req: CreateAppointmentRequest) = safeCall { api.createAppointment(req) }

    suspend fun checkInById(id: String) = safeCall { api.checkInById(id) }

    suspend fun checkInByCode(code: String) = safeCall { api.checkInByCode(CheckInByCodeRequest(code)) }

    suspend fun completeAppointment(id: String, resultNote: String?) =
        safeCall { api.completeAppointment(id, CompleteAppointmentRequest(resultNote)) }

    suspend fun cancelAppointment(id: String) = safeCall { api.cancelAppointment(id) }

    // ----- Payments -----
    suspend fun getPayments(customer: String? = null, customerPackage: String? = null) =
        safeCall { api.getPayments(customer, customerPackage) }

    suspend fun createPayment(req: CreatePaymentRequest) = safeCall { api.createPayment(req) }
}
