package com.analistainacap.misfinanzas.network

import retrofit2.Call
import retrofit2.http.*

interface SupabaseApiService {

    @POST("auth/v1/token?grant_type=password")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("rest/v1/vista_usuario_empresa_rol")
    fun getEmpresasConRol(): Call<List<EmpresaRolDTO>>

    @GET("rest/v1/vista_dashboard_financiero")
    fun getDashboard(@Query("empresa_id") empresaId: String): Call<List<DashboardDTO>>

    /**
     * Obtiene los últimos movimientos de una empresa.
     * Se asume la existencia de la vista 'vista_movimientos_detallados'.
     */
    @GET("rest/v1/vista_movimientos_detallados")
    fun getMovimientos(
        @Query("empresa_id") empresaId: String,
        @Query("order") order: String = "fecha.desc"
    ): Call<List<MovimientoDTO>>

    // --- CRUD EMPRESAS ---
    @GET("rest/v1/vista_empresas_usuario")
    fun getEmpresas(): Call<List<EmpresaDTO>>

    @POST("rest/v1/empresas")
    fun crearEmpresa(@Body empresa: EmpresaDTO): Call<Void>

    @PATCH("rest/v1/empresas")
    fun editarEmpresa(
        @Query("id") idFilter: String,
        @Body empresa: EmpresaDTO
    ): Call<Void>

    @POST("rest/v1/empresa_invitaciones")
    fun crearInvitacion(@Body request: InvitacionRequest): Call<Void>

    @POST("rest/v1/rpc/aceptar_invitacion")
    fun aceptarInvitacion(@Body body: Map<String, String>): Call<Void>

    @POST("rest/v1/rpc/crear_empresa")
    fun crearEmpresaRpc(@Body request: CreateEmpresaRequest): Call<String>
}
