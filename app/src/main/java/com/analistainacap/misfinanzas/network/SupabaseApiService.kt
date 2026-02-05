package com.analistainacap.misfinanzas.network

import retrofit2.Call
import retrofit2.http.*

/**
 * Interfaz de red senior para Supabase (PostgREST).
 * Contrato REST estricto para operaciones de escritura (Paso A3).
 */
interface SupabaseApiService {

    @POST("auth/v1/token?grant_type=password")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // --- EMPRESAS ---
    
    /**
     * 🔹 LISTADO DE EMPRESAS (TABLA BASE)
     * Consume directamente la tabla empresas con select explícito.
     */
    @GET("rest/v1/empresas?select=id,razon_social,rut_empresa,giro,tipo_empresa,fecha_inicio_actividades,direccion_comercial,correo_contacto,telefono_contacto,estado_empresa")
    fun getEmpresas(): Call<List<EmpresaDTO>>

    /**
     * 🔹 MIS ROLES (OBLIGATORIO)
     * Obtiene el rol y estado activo del usuario por cada empresa vinculada.
     */
    @GET("rest/v1/empresa_usuarios?select=empresa_id,rol,activo")
    fun getMisRoles(): Call<List<EmpresaRolDTO>>

    /**
     * 🔹 CREAR EMPRESA (RPC OBLIGATORIO)
     */
    @POST("rest/v1/rpc/crear_empresa")
    fun crearEmpresaRpc(@Body request: CreateEmpresaRequest): Call<String>

    /**
     * 🔹 EDITAR EMPRESA (PATCH)
     */
    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @PATCH("rest/v1/empresas")
    fun editarEmpresa(
        @QueryMap filtros: Map<String, String>,
        @Body campos: Map<String, @JvmSuppressWildcards Any?>
    ): Call<List<EmpresaDTO>>

    // --- MOVIMIENTOS ---
    
    @GET("rest/v1/vista_movimientos")
    fun getVistaMovimientos(
        @QueryMap filters: Map<String, String> = emptyMap(),
        @Header("Range") range: String? = null
    ): Call<List<MovimientoDTO>>

    // --- AUDITORÍA (C7) ---
    
    @GET("rest/v1/auditoria_contable")
    fun getAuditoria(
        @QueryMap filters: Map<String, String>
    ): Call<List<AuditoriaDTO>>

    // --- DASHBOARD Y RESÚMENES ---

    @GET("rest/v1/vista_kpi_resumen_mensual")
    fun getKpiResumenMensual(@QueryMap filters: Map<String, String>): Call<List<KpiResumenMensualDTO>>

    // --- CIERRE MENSUAL ---

    @GET("rest/v1/rpc/get_estado_cierre")
    fun getEstadoCierre(@QueryMap params: Map<String, String>): Call<List<CierreMensualDTO>>

    @POST("rest/v1/rpc/ejecutar_cierre_mensual")
    fun ejecutarCierre(@QueryMap params: Map<String, String>): Call<Void>

    @POST("rest/v1/rpc/reabrir_periodo")
    fun reabrirPeriodo(@QueryMap params: Map<String, String>): Call<Void>

    // --- CATÁLOGOS ---
    
    @GET("rest/v1/categorias")
    fun getCategorias(@QueryMap filters: Map<String, String> = mapOf("select" to "nombre")): Call<List<Map<String, String>>>

    // --- OTROS RPC ---
    
    @POST("rest/v1/rpc/aceptar_invitacion")
    fun aceptarInvitacion(@Body body: Map<String, String>): Call<Void>
}
