package com.spa.management.data.model

import com.google.gson.annotations.SerializedName

// ---------- Auth ----------
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val staff: StaffBrief)
data class StaffBrief(
    val id: String,
    val fullName: String,
    val username: String,
    val role: String
)
data class Staff(
    @SerializedName("_id") val id: String,
    val fullName: String,
    val username: String,
    val role: String,
    val phone: String? = null,
    val workingHours: String? = null,
    val active: Boolean = true
)

data class CreateStaffRequest(
    val fullName: String,
    val username: String,
    val password: String,
    val role: String, // admin | receptionist | technician
    val phone: String? = null,
    val workingHours: String? = null
)

data class CreateStaffResponse(
    val id: String,
    val fullName: String,
    val username: String
)

// ---------- Customer ----------
data class Customer(
    @SerializedName("_id") val id: String,
    val fullName: String,
    val phone: String,
    val dob: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val skinNotes: String? = null,
    val source: String? = null,
    val manualTag: String? = null,
    val tags: List<String> = emptyList(),
    val internalNotes: List<InternalNote> = emptyList(),
    val packages: List<CustomerPackage>? = null,
    val appointments: List<Appointment>? = null,
    val active: Boolean = true,
    val createdAt: String? = null
)

data class InternalNote(
    @SerializedName("_id") val id: String? = null,
    val date: String? = null,
    val content: String,
    val staff: StaffRef? = null
)

data class StaffRef(
    @SerializedName("_id") val id: String,
    val fullName: String
)

data class CreateCustomerRequest(
    val fullName: String,
    val phone: String,
    val dob: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val skinNotes: String? = null,
    val source: String? = null
)

data class AddNoteRequest(val content: String)

// ---------- Service Package (mau goi) ----------
data class ServicePackage(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String? = null,
    val services: List<String> = emptyList(),
    val totalSessions: Int,
    val durationDays: Int,
    val price: Double,
    val active: Boolean = true
)

data class CreateServicePackageRequest(
    val name: String,
    val description: String? = null,
    val services: List<String> = emptyList(),
    val totalSessions: Int,
    val durationDays: Int,
    val price: Double
)

// ---------- Customer Package (goi da gan cho khach) ----------
data class CustomerPackage(
    @SerializedName("_id") val id: String,
    val customer: Any? = null, // co the la String id hoac object populate {fullName, phone}
    val servicePackage: Any? = null, // String id hoac ServicePackage object
    val packageNameSnapshot: String,
    val priceSnapshot: Double,
    val sessionsTotal: Int,
    val sessionsUsed: Int,
    val sessionsRemaining: Int? = null,
    val startDate: String,
    val endDate: String,
    val type: String,
    val status: String,
    val lowSessions: Boolean? = null,
    val expiringSoon: Boolean? = null,
    val daysLeft: Int? = null
)

data class AssignPackageRequest(
    val customerId: String,
    val servicePackageId: String,
    val startDate: String? = null
)

data class RenewPackageRequest(
    val startDate: String? = null,
    val sessionsTotal: Int? = null,
    val durationDays: Int? = null
)

// ---------- Appointment ----------
data class Appointment(
    @SerializedName("_id") val id: String,
    val customer: Any? = null,
    val customerPackage: Any? = null,
    val technician: Any? = null,
    val room: String? = null,
    val serviceName: String? = null,
    val startTime: String,
    val durationMinutes: Int = 60,
    val status: String,
    val checkInTime: String? = null,
    val checkInCode: String? = null,
    val note: String? = null,
    val resultNote: String? = null
)

data class CreateAppointmentRequest(
    val customerId: String,
    val customerPackageId: String? = null,
    val technicianId: String? = null,
    val room: String? = null,
    val serviceName: String? = null,
    val startTime: String,
    val durationMinutes: Int = 60,
    val note: String? = null
)

data class CheckInByCodeRequest(val code: String)
data class CompleteAppointmentRequest(val resultNote: String?)

// ---------- Payment ----------
data class Payment(
    @SerializedName("_id") val id: String,
    val customer: Any? = null,
    val customerPackage: Any? = null,
    val amount: Double,
    val method: String,
    val date: String,
    val note: String? = null,
    val installment: InstallmentInfo? = null,
    val receivedBy: Any? = null
)

data class InstallmentInfo(
    val totalAmount: Double = 0.0,
    val installmentNumber: Int = 1,
    val totalInstallments: Int = 1
)

data class CreatePaymentRequest(
    val customerId: String,
    val customerPackageId: String? = null,
    val amount: Double,
    val method: String, // tien_mat | chuyen_khoan | tra_gop
    val note: String? = null,
    val installment: InstallmentInfo? = null
)

data class ApiMessage(val message: String)
