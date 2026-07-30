package com.spa.management.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatCurrency(amount: Double): String {
    val nf = NumberFormat.getInstance(Locale("vi", "VN"))
    return nf.format(amount) + " đ"
}

private val isoParsers = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'"
)

fun parseIsoDate(iso: String?): java.util.Date? {
    if (iso.isNullOrBlank()) return null
    for (pattern in isoParsers) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.parse(iso)
        } catch (e: Exception) {
            // thu pattern tiep theo
        }
    }
    return null
}

fun formatDate(iso: String?): String {
    val date = parseIsoDate(iso) ?: return "--"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
    return sdf.format(date)
}

fun formatDateTime(iso: String?): String {
    val date = parseIsoDate(iso) ?: return "--"
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale("vi", "VN"))
    return sdf.format(date)
}

fun isoNow(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(java.util.Date())
}

@Composable
fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// Nhan mau cho tung loai phan loai khach hang / trang thai
data class TagStyle(val label: String, val color: Color)

fun tagStyle(tag: String): TagStyle = when (tag) {
    "moi" -> TagStyle("Khách mới", Color(0xFF64B5F6))
    "dang_dung_lieu_trinh" -> TagStyle("Đang dùng liệu trình", Color(0xFF81C784))
    "vip" -> TagStyle("VIP", Color(0xFFFFD54F))
    "sap_het_buoi" -> TagStyle("Sắp hết buổi", Color(0xFFFFB74D))
    "da_het_buoi" -> TagStyle("Đã hết buổi", Color(0xFFE57373))
    "sap_het_han" -> TagStyle("Sắp hết hạn", Color(0xFFFF8A65))
    "da_het_han" -> TagStyle("Đã hết hạn", Color(0xFFBDBDBD))
    "moi_mua_goi" -> TagStyle("Mới mua gói", Color(0xFF4FC3F7))
    else -> TagStyle(tag, Color(0xFFB0BEC5))
}

fun appointmentStatusLabel(status: String): String = when (status) {
    "booked" -> "Đã đặt lịch"
    "checked_in" -> "Đã check-in"
    "completed" -> "Hoàn thành"
    "cancelled" -> "Đã hủy"
    "no_show" -> "Không đến"
    else -> status
}

fun packageStatusLabel(status: String): String = when (status) {
    "active" -> "Đang hoạt động"
    "completed" -> "Đã hoàn thành"
    "expired" -> "Đã hết hạn"
    "cancelled" -> "Đã hủy"
    else -> status
}

fun paymentMethodLabel(method: String): String = when (method) {
    "tien_mat" -> "Tiền mặt"
    "chuyen_khoan" -> "Chuyển khoản"
    "tra_gop" -> "Trả góp"
    else -> method
}
