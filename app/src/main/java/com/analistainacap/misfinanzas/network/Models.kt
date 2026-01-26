package com.analistainacap.misfinanzas.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// --- Auth ---
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val access_token: String)

// --- Empresas (Compatible con Tabla y Vistas) ---
data class EmpresaDTO(
    @SerializedName(value = "id", alternate = ["empresa_id"])
    val id: String? = null,
    
    @SerializedName(value = "razon_social", alternate = ["empresa_nombre", "nombre"])
    val razonSocial: String,
    
    @SerializedName("rut_empresa") 
    val rutEmpresa: String,
    
    val giro: String,
    
    @SerializedName("tipo_empresa") 
    val tipoEmpresa: String? = null,
    
    @SerializedName("fecha_inicio_actividades") 
    val fechaInicio: String? = null,
    
    @SerializedName(value = "direccion_comercial", alternate = ["direccion"])
    val direccionComercial: String? = null,
    
    val comuna: String? = null,
    val region: String? = null,
    
    @SerializedName(value = "correo_contacto", alternate = ["correo"])
    val correoContacto: String? = null,
    
    @SerializedName(value = "telefono_contacto", alternate = ["telefono"])
    val telefonoContacto: String? = null,
    
    @SerializedName("estado_empresa") 
    val estadoEmpresa: String? = "activa",
    
    val activa: Boolean? = true
) : Serializable

// --- Creación de Empresa (Parámetros para RPC) ---
data class CreateEmpresaRequest(
    val razon_social: String,
    val rut_empresa: String,
    val giro: String,
    val tipo_empresa: String,
    val fecha_inicio_actividades: String,
    val direccion_comercial: String,
    val comuna: String,
    val region: String,
    val correo_contacto: String,
    val telefono_contacto: String,
    val representante_legal: String,
    val regimen_tributario: String,
    val afecta_iva: Boolean
)

// --- Dashboard y Otros ---
data class EmpresaRolDTO(
    val empresa_id: String,
    val empresa_nombre: String,
    val rol: String
)

data class DashboardDTO(
    @SerializedName("empresa_id") val empresaId: String,
    val nombre: String,
    @SerializedName("total_ingresos") val ingresos: Double,
    @SerializedName("total_gastos") val gastos: Double,
    @SerializedName("saldo_real") val saldoReal: Double
)

// --- Movimientos ---
data class MovimientoDTO(
    val id: String,
    val fecha: String,
    val glosa: String,
    val monto: Double,
    @SerializedName("tipo_movimiento") val tipo: String, // "ingreso" o "egreso"
    @SerializedName("categoria_nombre") val categoria: String? = null
)

data class ResumenMensualDTO(
    @SerializedName("empresa_id") val empresaId: String,
    val periodo: String,
    @SerializedName("total_ingresos") val totalIngresos: Double,
    @SerializedName("total_gastos") val totalGastos: Double,
    val saldo: Double
)

data class InvitacionRequest(
    val email: String,
    val empresa_id: String,
    val rol: String
)
