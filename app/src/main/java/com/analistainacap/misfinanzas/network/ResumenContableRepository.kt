package com.analistainacap.misfinanzas.network

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repositorio para KPIs Contables (C4).
 * Incluye instrumentación de LOGS (G1) para verificar respuesta de Supabase.
 */
class ResumenContableRepository(private val apiService: SupabaseApiService) {

    private val TAG = "SupabaseDebug"

    fun getKpisGenerales(
        empresaId: String,
        periodo: String,
        onResult: (KpiResumenMensualDTO?) -> Unit,
        onError: (String) -> Unit
    ) {
        val filters = mapOf(
            "empresa_id" to "eq.$empresaId",
            "periodo" to "eq.$periodo"
        )
        
        Log.d(TAG, "G1 - Request KPIs Contables: $filters")

        apiService.getKpiResumenMensual(filters)
            .enqueue(object : Callback<List<KpiResumenMensualDTO>> {
                override fun onResponse(
                    call: Call<List<KpiResumenMensualDTO>>,
                    response: Response<List<KpiResumenMensualDTO>>
                ) {
                    Log.d(TAG, "G1 - Response KPIs Contables: Code=${response.code()}, Body=${response.body()}")
                    if (response.isSuccessful) {
                        onResult(response.body()?.firstOrNull())
                    } else {
                        onError("Error KPI: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<KpiResumenMensualDTO>>, t: Throwable) {
                    Log.e(TAG, "G1 - Fallo conexión KPIs Contables", t)
                    onError("Fallo de red KPIs")
                }
            })
    }

    fun getIvaResumen(
        empresaId: String,
        periodo: String,
        onResult: (KpiResumenMensualDTO?) -> Unit,
        onError: (String) -> Unit
    ) {
        val filters = mapOf(
            "empresa_id" to "eq.$empresaId",
            "periodo" to "eq.$periodo"
        )

        Log.d(TAG, "G1 - Request IVA: $filters")

        // Sincronizado con SupabaseApiService: IVA y KPIs comparten el DTO consolidado (A3)
        apiService.getKpiResumenMensual(filters)
            .enqueue(object : Callback<List<KpiResumenMensualDTO>> {
                override fun onResponse(
                    call: Call<List<KpiResumenMensualDTO>>,
                    response: Response<List<KpiResumenMensualDTO>>
                ) {
                    Log.d(TAG, "G1 - Response IVA: Code=${response.code()}, Body=${response.body()}")
                    if (response.isSuccessful) {
                        onResult(response.body()?.firstOrNull())
                    } else {
                        onError("Error IVA: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<KpiResumenMensualDTO>>, t: Throwable) {
                    Log.e(TAG, "G1 - Fallo conexión IVA", t)
                    onError("Fallo de red IVA")
                }
            })
    }
}
