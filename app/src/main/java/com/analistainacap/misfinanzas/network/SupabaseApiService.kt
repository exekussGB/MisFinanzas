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

    @GET("rest/v1/vista_resumen_mensual")
    fun getResumenMensual(@Query("empresa_id") empresaId: String): Call<List<ResumenMensualDTO>>

    @POST("rest/v1/empresa_invitaciones")
    fun crearInvitacion(@Body request: InvitacionRequest): Call<Void>

    @POST("rest/v1/rpc/aceptar_invitacion")
    fun aceptarInvitacion(@Body body: Map<String, String>): Call<Void>

    @POST("rest/v1/rpc/crear_empresa")
    fun crearEmpresa(@Body request: CreateEmpresaRequest): Call<String>
}
