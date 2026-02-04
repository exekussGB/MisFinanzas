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
    
    @GET("rest/v1/vista_empresas_usuario")
    fun getEmpresas(): Call<List<EmpresaDTO>>

    /**
     * 🔹 CREAR EMPRESA (POST)
     * Envía exclusivamente el objeto JSON en el Body.
     */
    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("rest/v1/empresas")
    fun crearEmpresa(
        @Body request: CreateEmpresaRequest
    ): Call<List<EmpresaDTO>>

    /**
     * 🔹 EDITAR EMPRESA (PATCH)
     * Filtros dinámicos por URL (@QueryMap) y campos a actualizar en el Body.
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

    @GET("rest/v1/vista_movimientos_detallados")
    fun getMovimientos(
        @QueryMap filters: Map<String, String> = emptyMap(),
        @Header("Range") range: String? = null
    ): Call<List<MovimientoDTO>>

    // --- AUDITORÍA (C7) ---
    
    @GET("rest/v1/auditoria_contable")
    fun getAuditoria(
        @QueryMap filters: Map<String, String> = emptyMap(),
        @Header("Range") range: String? = "0-49",
        @Query("order") order: String = "fecha_evento.desc"
    ): Call<List<AuditoriaDTO>>

    // --- DASHBOARD Y RESÚMENES (KPIs) ---

    @GET("rest/v1/vista_kpis_empresa")
    fun getKpis(@QueryMap filters: Map<String, String>): Call<List<KpisEmpresaDTO>>

    @GET("rest/v1/vista_resumen_movimientos")
    fun getResumenMovimientos(@QueryMap filters: Map<String, String>): Call<List<ResumenMovimientoDTO>>

    @GET("rest/v1/vista_saldo_financiero")
    fun getSaldosFormaPago(@QueryMap filters: Map<String, String>): Call<List<SaldoFormaPagoDTO>>

    @GET("rest/v1/vista_kpi_resumen_mensual")
    fun getKpiResumenMensual(@QueryMap filters: Map<String, String>): Call<List<KpiResumenMensualDTO>>

    @GET("rest/v1/vista_iva_resumen_mensual")
    fun getIvaResumenMensual(@QueryMap filters: Map<String, String>): Call<List<KpiResumenMensualDTO>>

    @GET("rest/v1/vista_resumen_categorias")
    fun getResumenCategorias(@QueryMap filters: Map<String, String>): Call<List<ResumenCategoriaDTO>>

    // --- CIERRE MENSUAL (BLOQUE B4-B) ---

    @GET("rest/v1/rpc/get_estado_cierre")
    fun getEstadoCierre(
        @QueryMap params: Map<String, String>
    ): Call<List<CierreMensualDTO>>

    @POST("rest/v1/rpc/ejecutar_cierre_mensual")
    fun ejecutarCierre(
        @QueryMap params: Map<String, String>
    ): Call<Void>

    @POST("rest/v1/rpc/reabrir_periodo")
    fun reabrirPeriodo(
        @QueryMap params: Map<String, String>
    ): Call<Void>

    // --- CATÁLOGOS ---
    
    @GET("rest/v1/categorias")
    fun getCategorias(@QueryMap filters: Map<String, String> = mapOf("select" to "nombre")): Call<List<Map<String, String>>>

    @GET("rest/v1/cuentas")
    fun getCuentas(@QueryMap filters: Map<String, String> = mapOf("select" to "nombre")): Call<List<Map<String, String>>>

    @GET("rest/v1/formas_pago")
    fun getFormasPago(@QueryMap filters: Map<String, String> = mapOf("select" to "nombre")): Call<List<Map<String, String>>>

    // --- OTROS RPC ---
    
    @POST("rest/v1/rpc/crear_empresa")
    fun crearEmpresaRpc(@Body request: CreateEmpresaRequest): Call<String>

    @POST("rest/v1/rpc/aceptar_invitacion")
    fun aceptarInvitacion(@Body body: Map<String, String>): Call<Void>

    @POST("rest/v1/empresa_invitaciones")
    fun crearInvitacion(@Body request: InvitacionRequest): Call<Void>
}
