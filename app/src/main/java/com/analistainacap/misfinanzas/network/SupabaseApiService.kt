package com.analistainacap.misfinanzas.network

import retrofit2.Call
import retrofit2.http.*

/**
 * Interfaz de red senior para Supabase (PostgREST).
 * Todas las operaciones de escritura usan @Body con JSON válido.
 */
interface SupabaseApiService {

    @POST("auth/v1/token?grant_type=password")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // --- EMPRESAS ---
    
    @GET("rest/v1/vista_empresas_usuario")
    fun getEmpresas(): Call<List<EmpresaDTO>>

    /**
     * Creación de empresa (POST).
     * Obligatorio: Content-Type: application/json
     * Prefer: return=representation para obtener el objeto creado.
     */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/empresas")
    fun crearEmpresa(@Body empresa: CreateEmpresaRequest): Call<List<EmpresaDTO>>

    /**
     * Actualización parcial de empresa (PATCH).
     * El ID se pasa como filtro en la URL, los datos en el BODY.
     */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/empresas")
    fun editarEmpresa(
        @Query("id") idFilter: String,
        @Body empresa: Map<String, @JvmSuppressWildcards Any>
    ): Call<List<EmpresaDTO>>

    // --- MOVIMIENTOS ---
    
    @GET("rest/v1/vista_movimientos")
    fun getVistaMovimientos(
        @QueryMap filters: Map<String, String> = emptyMap(),
        @Header("Range") range: String? = null
    ): Call<List<MovimientoDTO>>

    // --- KPIs Y REPORTES ---

    @GET("rest/v1/vista_kpi_resumen_mensual")
    fun getKpiResumenMensual(@QueryMap filters: Map<String, String>): Call<List<KpiResumenMensualDTO>>

    @GET("rest/v1/vista_iva_resumen_mensual")
    fun getIvaResumenMensual(@QueryMap filters: Map<String, String>): Call<List<KpiResumenMensualDTO>>

    @GET("rest/v1/vista_resumen_categorias")
    fun getResumenCategorias(@QueryMap filters: Map<String, String>): Call<List<ResumenCategoriaDTO>>

    // --- AUDITORÍA Y OTROS ---

    @GET("rest/v1/auditoria_contable")
    fun getAuditoria(
        @QueryMap filters: Map<String, String> = emptyMap(),
        @Header("Range") range: String? = null
    ): Call<List<AuditoriaDTO>>

    @POST("rest/v1/rpc/validar_cierre_mensual")
    fun validarCierre(@Body params: Map<String, Any>): Call<Map<String, Any>>

    @POST("rest/v1/rpc/crear_empresa")
    fun crearEmpresaRpc(@Body request: CreateEmpresaRequest): Call<String>

    @POST("rest/v1/rpc/aceptar_invitacion")
    fun aceptarInvitacion(@Body body: Map<String, String>): Call<Void>
}
