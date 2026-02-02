package com.analistainacap.misfinanzas.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de Sesión y Permisos (C8).
 * Centraliza el acceso al rol del usuario y empresa activa.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    companion object {
        const val KEY_TOKEN = "token"
        const val KEY_USER_ROL = "user_rol"
        const val KEY_EMPRESA_ID = "empresa_id_activa"
        
        // Roles Definidos (C8.2)
        const val ROLE_OWNER = "owner"
        const val ROLE_ADMIN = "administrador"
        const val ROLE_CONTADOR = "contador"
        const val ROLE_USUARIO = "usuario"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun saveRol(rol: String) {
        prefs.edit().putString(KEY_USER_ROL, rol.lowercase()).apply()
    }

    fun getRol(): String = prefs.getString(KEY_USER_ROL, ROLE_USUARIO) ?: ROLE_USUARIO

    fun getEmpresaId(): String = prefs.getString(KEY_EMPRESA_ID, "") ?: ""

    // --- Lógica de Permisos (C8.5) ---

    fun puedeExportar(): Boolean {
        val rol = getRol()
        return rol == ROLE_OWNER || rol == ROLE_ADMIN || rol == ROLE_CONTADOR
    }

    fun puedeVerAuditoria(): Boolean {
        val rol = getRol()
        return rol == ROLE_OWNER || rol == ROLE_ADMIN || rol == ROLE_CONTADOR
    }

    fun puedeGestionarEmpresa(): Boolean {
        val rol = getRol()
        return rol == ROLE_OWNER || rol == ROLE_ADMIN
    }

    fun puedeEliminarEmpresa(): Boolean {
        return getRol() == ROLE_OWNER // Solo el owner puede borrar (C8.5)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
