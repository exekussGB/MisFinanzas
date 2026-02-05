package com.analistainacap.misfinanzas.network

import retrofit2.Call
import retrofit2.http.*

/**
 * Interfaz de red senior para Supabase (PostgREST).
 */
interface SupabaseApiService {

    @POST("auth/v1/token?grant_type=password")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // --- EMPRESAS ---
    
    @GET("rest/v1/empresas?select=*")
    fun getEmpresas(): Call<List<EmpresaDTO>>

    @GET("rest/v1/empresa_usuarios?select=empresa_id,rol,activo")
    fun getMisRoles(): Call<List<EmpresaRolDTO>>

    /**
     * ✅ ANDROID — ENDPOINT CORRECTO
     */
    @POST("rest/v1/rpc/crear_empresa")
    fun crearEmpresaRpc(
        @Body request: CreateEmpresaRequest
    ): Call<UUIDResponse>

    @PATCH("rest/v1/empresas")
    fun editarEmpresa(
        @Query("id") idFilter: String,
        @Body body: UpdateEmpresaRequest
    ): Call<Void>

    // --- MOVIMIENTOS ---
    
    @GET("rest/v1/vista_movimientos")
    fun getVistaMovimientos(
        @QueryMap filters: Map<String, String> = emptyMap(),
        @Header("Range") range: String? = null
    ): Call<List<MovimientoDTO>>

    // --- AUDITORÍA ---
    
    @GET("rest/v1/auditoria_contable")
    fun getAuditoria(@QueryMap filters: Map<String, String>): Call<List<AuditoriaDTO>>

    // --- KPIs ---

    @GET("rest/v1/vista_kpi_resumen_mensual")
    fun getKpiResumenMensual(@QueryMap filters: Map<String, String>): Call<List<KpiResumenMensualDTO>>

    // --- CIERRE MENSUAL ---

    @GET("rest/v1/rpc/get_estado_cierre")
    fun getEstadoCierre(@QueryMap params: Map<String, String>): Call<List<CierreMensualDTO>>

    @POST("rest/v1/rpc/ejecutar_cierre_mensual")
    fun ejecutarCierre(@QueryMap params: Map<String, String>): Call<Void>

    @POST("rest/v1/rpc/reabrir_periodo")
    fun reabrirPeriodo(@QueryMap params: Map<String, String>): Call<Void>

    @GET("rest/v1/categorias")
    fun getCategorias(@QueryMap filters: Map<String, String> = mapOf("select" to "nombre")): Call<List<Map<String, String>>>

    @POST("rest/v1/rpc/aceptar_invitacion")
    fun aceptarInvitacion(@Body body: Map<String, String>): Call<Void>
}
