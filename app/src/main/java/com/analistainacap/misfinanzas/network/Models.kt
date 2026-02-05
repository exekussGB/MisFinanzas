package com.analistainacap.misfinanzas.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LoginRequest(
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?
) : Serializable

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String?
) : Serializable

data class UUIDResponse(
    @SerializedName("id") val id: String
) : Serializable

data class EmpresaUsuarioRolDTO(
    @SerializedName("rol") val rol: String?
) : Serializable

data class EmpresaDTO(
    @SerializedName("id") val id: String?,
    @SerializedName("razon_social") val razonSocial: String?,
    @SerializedName("rut_empresa") val rutEmpresa: String?,
    @SerializedName("giro") val giro: String?,
    @SerializedName("tipo_empresa") val tipoEmpresa: String?,
    @SerializedName("fecha_inicio_actividades") val fechaInicioActividades: String?,
    @SerializedName("direccion_comercial") val direccionComercial: String?,
    @SerializedName("correo_contacto") val correoContacto: String?,
    @SerializedName("telefono_contacto") val telefonoContacto: String?,
    @SerializedName("estado_empresa") val estadoEmpresa: String?,
    @SerializedName("rol") var rol: String?,
    @SerializedName("activa") val activa: Boolean? = true,
    @SerializedName("empresa_usuarios") val empresaUsuarios: List<EmpresaUsuarioRolDTO>? = null
) : Serializable

/**
 * 🔧 BLOQUE 2 — UpdateEmpresaRequest (Models.kt)
 * DTO exclusivo para edición (PATCH).
 */
data class UpdateEmpresaRequest(
    val razon_social: String,
    val rut_empresa: String,
    val giro: String?,
    val tipo_empresa: String?,
    val direccion_comercial: String?,
    val correo_contacto: String?,
    val telefono_contacto: String?
) : Serializable

/**
 * 2️⃣ DTO PARA CREAR EMPRESA (RPC)
 */
data class CreateEmpresaRequest(
    @SerializedName("p_razon_social") val razon_social: String,
    @SerializedName("p_rut_empresa") val rut_empresa: String,
    @SerializedName("p_giro") val giro: String?,
    @SerializedName("p_tipo_empresa") val tipo_empresa: String?,
    @SerializedName("p_direccion_comercial") val direccion_comercial: String?,
    @SerializedName("p_correo_contacto") val correo_contacto: String?,
    @SerializedName("p_telefono_contacto") val telefono_contacto: String?
) : Serializable

data class AuditoriaDTO(
    @SerializedName("id") val id: String?,
    @SerializedName("tabla_nombre") val tablaNombre: String?,
    @SerializedName("operacion") val operacion: String?,
    @SerializedName("usuario_email") val usuarioEmail: String?,
    @SerializedName("fecha_evento") val fechaEvento: String?,
    @SerializedName("dato_anterior") val datoAnterior: String?,
    @SerializedName("dato_nuevo") val datoNuevo: String?,
    @SerializedName("empresa_id") val empresaId: String?
) : Serializable

data class CierreMensualDTO(
    @SerializedName("empresa_id") val empresaId: String?,
    @SerializedName("mes") val mes: Int?,
    @SerializedName("anio") val anio: Int?,
    @SerializedName("cerrado") val cerrado: Boolean?,
    @SerializedName("fecha_cierre") val fechaCierre: String?,
    @SerializedName("cerrado_por") val cerradoPor: String?
) : Serializable

data class KpiResumenMensualDTO(
    @SerializedName("empresa_id") val empresaId: String?,
    @SerializedName("periodo") val periodo: String?,
    @SerializedName("total_ingresos") val totalIngresos: Double?,
    @SerializedName("total_egresos") val totalEgresos: Double?,
    @SerializedName("resultado_periodo") val resultadoPeriodo: Double?,
    @SerializedName("ventas_netas") val ventasNetas: Double?,
    @SerializedName("iva_debito") val ivaDebito: Double?,
    @SerializedName("iva_credito") val ivaCredito: Double?,
    @SerializedName("iva_por_pagar") val ivaPorPagar: Double?,
    @SerializedName("remanente_iva") val remanenteIva: Double?
) : Serializable

data class ResumenCategoriaDTO(
    @SerializedName("categoria_nombre") val categoriaNombre: String?,
    @SerializedName("tipo_movimiento") val tipoMovimiento: String?,
    @SerializedName("total") val total: Double?,
    @SerializedName("cantidad_movimientos") val cantidadMovimientos: Int?
) : Serializable

data class ResumenMovimientoDTO(
    @SerializedName("tipo") val tipo: String?,
    @SerializedName("total") val total: Double?
) : Serializable

data class SaldoFormaPagoDTO(
    @SerializedName("forma_pago") val formaPago: String?,
    @SerializedName("saldo") val saldo: Double?
) : Serializable

data class KpisEmpresaDTO(
    @SerializedName("saldo_financiero") val saldoFinanciero: Double?,
    @SerializedName("saldo_contable") val saldoContable: Double?,
    @SerializedName("diferencia") val diferencia: Double?
) : Serializable

data class MovimientoDTO(
    @SerializedName("id") val id: String?,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("glosa") val glosa: String?,
    @SerializedName("monto") val monto: Double?,
    @SerializedName("tipo_movimiento") val tipoMovimiento: String?,
    @SerializedName("categoria_nombre") val categoriaNombre: String?,
    @SerializedName("forma_pago") val formaPago: String?,
    @SerializedName("monto_neto") val montoNeto: Double?,
    @SerializedName("monto_iva") val montoIva: Double?,
    @SerializedName("afecta_iva") val afectaIva: Boolean?,
    @SerializedName("cuenta") val cuenta: String?,
    @SerializedName("proveedor") val proveedor: String?,
    @SerializedName("cliente") val cliente: String?,
    @SerializedName("documento_hash") val documentoHash: String?
) : Serializable

data class InvitacionRequest(
    @SerializedName("email") val email: String?,
    @SerializedName("empresa_id") val empresaId: String?,
    @SerializedName("rol") val rol: String?
) : Serializable

data class EmpresaRolDTO(
    @SerializedName("empresa_id") val empresaId: String?,
    @SerializedName("empresa_nombre") val empresaNombre: String?,
    @SerializedName("rol") val rol: String?
) : Serializable
