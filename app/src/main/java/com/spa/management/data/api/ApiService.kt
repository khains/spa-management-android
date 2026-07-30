package com.spa.management.data.api

import com.spa.management.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------- Auth ----------
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/auth/staff")
    suspend fun getStaffList(): Response<List<Staff>>

    @POST("api/auth/staff")
    suspend fun createStaff(@Body body: CreateStaffRequest): Response<CreateStaffResponse>

    // ---------- Customers ----------
    @GET("api/customers")
    suspend fun getCustomers(
        @Query("search") search: String? = null,
        @Query("tag") tag: String? = null
    ): Response<List<Customer>>

    @GET("api/customers/{id}")
    suspend fun getCustomerDetail(@Path("id") id: String): Response<Customer>

    @POST("api/customers")
    suspend fun createCustomer(@Body body: CreateCustomerRequest): Response<Customer>

    @PUT("api/customers/{id}")
    suspend fun updateCustomer(@Path("id") id: String, @Body body: Map<String, @JvmSuppressWildcards Any?>): Response<Customer>

    @DELETE("api/customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: String): Response<ApiMessage>

    @POST("api/customers/{id}/notes")
    suspend fun addCustomerNote(@Path("id") id: String, @Body body: AddNoteRequest): Response<InternalNote>

    // ---------- Service Packages (mau goi) ----------
    @GET("api/packages")
    suspend fun getServicePackages(): Response<List<ServicePackage>>

    @POST("api/packages")
    suspend fun createServicePackage(@Body body: CreateServicePackageRequest): Response<ServicePackage>

    @PUT("api/packages/{id}")
    suspend fun updateServicePackage(@Path("id") id: String, @Body body: Map<String, @JvmSuppressWildcards Any?>): Response<ServicePackage>

    @DELETE("api/packages/{id}")
    suspend fun deleteServicePackage(@Path("id") id: String): Response<ApiMessage>

    // ---------- Customer Packages (goi cua khach) ----------
    @GET("api/customer-packages")
    suspend fun getCustomerPackages(
        @Query("status") status: String? = null,
        @Query("customer") customer: String? = null,
        @Query("lowSessions") lowSessions: Boolean? = null,
        @Query("expiringSoon") expiringSoon: Boolean? = null
    ): Response<List<CustomerPackage>>

    @POST("api/customer-packages")
    suspend fun assignPackage(@Body body: AssignPackageRequest): Response<CustomerPackage>

    @POST("api/customer-packages/{id}/renew")
    suspend fun renewPackage(@Path("id") id: String, @Body body: RenewPackageRequest): Response<CustomerPackage>

    @GET("api/customer-packages/{id}")
    suspend fun getCustomerPackageDetail(@Path("id") id: String): Response<CustomerPackage>

    // ---------- Appointments ----------
    @GET("api/appointments")
    suspend fun getAppointments(
        @Query("date") date: String? = null,
        @Query("technician") technician: String? = null,
        @Query("status") status: String? = null,
        @Query("customer") customer: String? = null
    ): Response<List<Appointment>>

    @GET("api/appointments/availability")
    suspend fun getAvailability(
        @Query("technician") technician: String,
        @Query("date") date: String
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("api/appointments")
    suspend fun createAppointment(@Body body: CreateAppointmentRequest): Response<Appointment>

    @PUT("api/appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: String, @Body body: Map<String, @JvmSuppressWildcards Any?>): Response<Appointment>

    @POST("api/appointments/{id}/checkin")
    suspend fun checkInById(@Path("id") id: String): Response<Appointment>

    @POST("api/appointments/checkin-by-code")
    suspend fun checkInByCode(@Body body: CheckInByCodeRequest): Response<Appointment>

    @POST("api/appointments/{id}/complete")
    suspend fun completeAppointment(@Path("id") id: String, @Body body: CompleteAppointmentRequest): Response<Appointment>

    @POST("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") id: String): Response<Appointment>

    // ---------- Payments ----------
    @GET("api/payments")
    suspend fun getPayments(
        @Query("customer") customer: String? = null,
        @Query("customerPackage") customerPackage: String? = null
    ): Response<List<Payment>>

    @POST("api/payments")
    suspend fun createPayment(@Body body: CreatePaymentRequest): Response<Payment>
}
