package com.analistainacap.misfinanzas.network

/**
 * Modelo para la respuesta de una invitación.
 * El token es generado por Supabase para el proceso de registro vía email.
 */
data class InvitacionResponse(
    val token: String
)
