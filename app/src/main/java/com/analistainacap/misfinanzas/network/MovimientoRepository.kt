package com.analistainacap.misfinanzas.network

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repositorio para la gestión de Movimientos.
 * Centraliza las llamadas a Supabase y abstrae la fuente de datos.
 * Incluye instrumentación de LOGS (G1) para depuración de respuestas crudas.
 */
class MovimientoRepository(private val apiService: SupabaseApiService) {

    private val TAG = "SupabaseDebug"

    fun getDetalleMovimiento(
        empresaId: String,
        movimientoId: String,
        onResult: (MovimientoDTO?) -> Unit,
        onError: (String) -> Unit
    ) {
        val filters = mapOf("empresa_id" to "eq.$empresaId")
        
        Log.d(TAG, "G1 - Request Detalle Movimiento: $filters")

        apiService.getVistaMovimientos(filters).enqueue(object : Callback<List<MovimientoDTO>> {
            override fun onResponse(call: Call<List<MovimientoDTO>>, response: Response<List<MovimientoDTO>>) {
                Log.d(TAG, "G1 - Response Detalle Movimiento: Code=${response.code()}, Body=${response.body()}")
                
                if (response.isSuccessful) {
                    val movimiento = response.body()?.find { it.id == movimientoId }
                    onResult(movimiento)
                } else {
                    onError("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<MovimientoDTO>>, t: Throwable) {
                Log.e(TAG, "G1 - Fallo conexión Detalle Movimiento", t)
                onError("Fallo de conexión: ${t.message}")
            }
        })
    }
}
