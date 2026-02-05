package com.analistainacap.misfinanzas.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de Sesión y Permisos (C8).
 * Centraliza el acceso al rol del usuario, id de usuario y empresa activa.
 * Refactorizado: Los permisos se basan en la jerarquía funcional, no en strings rígidos.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    companion object {
        const val KEY_TOKEN = "token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_ROL = "user_rol"
        const val KEY_EMPRESA_ID = "empresa_id_activa"
        
        // Roles Funcionales
        const val ROLE_OWNER = "owner"
        const val ROLE_CONTADOR = "contador"
        const val ROLE_USUARIO = "usuario"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun saveRol(rol: String) {
        prefs.edit().putString(KEY_USER_ROL, rol.lowercase()).apply()
    }

    fun getUserId(): String = prefs.getString(KEY_USER_ID, "") ?: ""

    fun getRol(): String = prefs.getString(KEY_USER_ROL, ROLE_USUARIO) ?: ROLE_USUARIO

    fun getEmpresaId(): String = prefs.getString(KEY_EMPRESA_ID, "") ?: ""

    // --- Lógica de Permisos Refactorizada (Quitar dependencias de Admin) ---

    fun puedeExportar(): Boolean {
        val rol = getRol()
        return rol == ROLE_OWNER || rol == ROLE_CONTADOR
    }

    fun puedeVerAuditoria(): Boolean {
        val rol = getRol()
        return rol == ROLE_OWNER || rol == ROLE_CONTADOR
    }

    fun puedeGestionarEmpresa(): Boolean {
        return getRol() == ROLE_OWNER
    }

    fun puedeEliminarEmpresa(): Boolean {
        return getRol() == ROLE_OWNER
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
