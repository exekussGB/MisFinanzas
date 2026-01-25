package com.analistainacap.misfinanzas.network

import com.google.gson.annotations.SerializedName

// --- Auth ---
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val access_token: String)

// --- Empresas ---
data class EmpresaRolDTO(
    val empresa_id: String,
    val empresa_nombre: String,
    val rol: String
)

// --- Dashboard ---
data class DashboardDTO(
    @SerializedName("empresa_id") val empresaId: String,
    val nombre: String,
    @SerializedName("total_ingresos") val ingresos: Double,
    @SerializedName("total_gastos") val gastos: Double,
    @SerializedName("saldo_real") val saldoReal: Double
)

// --- Resumen Mensual ---
data class ResumenMensualDTO(
    @SerializedName("empresa_id") val empresaId: String,
    val periodo: String,
    @SerializedName("total_ingresos") val totalIngresos: Double,
    @SerializedName("total_gastos") val totalGastos: Double,
    val saldo: Double
)

// --- Invitaciones ---
data class InvitacionRequest(
    val email: String,
    val empresa_id: String,
    val rol: String
)

// --- Nueva Empresa ---
data class CreateEmpresaRequest(
    val razon_social: String,
    val rut_empresa: String,
    val giro: String,
    val tipo_empresa: String,
    val fecha_inicio_actividades: String, // Formato YYYY-MM-DD
    val direccion: String,
    val comuna: String,
    val region: String,
    val correo: String,
    val telefono: String,
    val representante_legal: String,
    val regimen_tributario: String,
    val afecta_iva: Boolean
)
