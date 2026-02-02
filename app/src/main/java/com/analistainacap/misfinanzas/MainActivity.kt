package com.analistainacap.misfinanzas

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.analistainacap.misfinanzas.databinding.ActivityMainBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

/**
 * Dashboard Financiero (C9).
 * Gestión de estados de carga y logs de red (G1).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val KEY_EMPRESA_ID_PREFS = "empresa_id_activa"
    private val TAG = "SupabaseDebug"
    
    private var empresaIdActiva = ""
    private var periodoActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        empresaIdActiva = intent.getStringExtra("empresa_id") ?: prefs.getString(KEY_EMPRESA_ID_PREFS, "") ?: ""
        val empresaNombre = prefs.getString("empresa_nombre", "Dashboard") ?: "Dashboard"

        if (empresaIdActiva.isEmpty()) {
            Toast.makeText(this, "Identificador de empresa no disponible", Toast.LENGTH_LONG).show()
            binding.pbLoadingDashboard.visibility = View.GONE
            return
        }

        prefs.edit().putString(KEY_EMPRESA_ID_PREFS, empresaIdActiva).apply()

        val cal = Calendar.getInstance()
        updatePeriodo(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

        binding.tvEmpresaNombre.text = empresaNombre
        binding.rvMovimientos.layoutManager = LinearLayoutManager(this)

        cargarDashboard()

        binding.tvEmpresaNombre.setOnClickListener { showMonthYearPicker() }
        binding.btnLogout.setOnClickListener { forceLogout() }
        binding.btnReintentarDashboard.setOnClickListener { cargarDashboard() }
    }

    private fun updatePeriodo(year: Int, month: Int) {
        periodoActual = String.format("%d-%02d", year, month)
    }

    private fun cargarDashboard() {
        Log.d(TAG, "API llamada: Iniciando carga de Dashboard")
        binding.pbLoadingDashboard.visibility = View.VISIBLE
        binding.scrollDashboard.visibility = View.GONE
        binding.layoutErrorDashboard.visibility = View.GONE

        val filters = mutableMapOf(
            "empresa_id" to "eq.$empresaIdActiva"
        )
        fetchKPIsPrincipales(filters)
        fetchUltimosMovimientos(empresaIdActiva)
    }

    private fun showMonthYearPicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, _ ->
            updatePeriodo(year, month + 1)
            val manualFilters = mapOf(
                "empresa_id" to "eq.$empresaIdActiva",
                "periodo" to "eq.$periodoActual"
            )
            fetchKPIsPrincipales(manualFilters)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1).show()
    }

    private fun fetchKPIsPrincipales(filters: Map<String, String>) {
        Log.d(TAG, "API llamada: fetchKPIsPrincipales")
        RetrofitClient.getApi(this).getKpiResumenMensual(filters)
            .enqueue(object : Callback<List<KpiResumenMensualDTO>> {
                override fun onResponse(call: Call<List<KpiResumenMensualDTO>>, response: Response<List<KpiResumenMensualDTO>>) {
                    Log.d(TAG, "onResponse ejecutado: KPIs")
                    binding.pbLoadingDashboard.visibility = View.GONE
                    binding.scrollDashboard.visibility = View.VISIBLE

                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        renderKPIs(response.body()!![0])
                    } else {
                        Log.w(TAG, "KPIs: Éxito pero sin datos o error de servidor")
                        limpiarKPIs()
                    }
                }

                override fun onFailure(call: Call<List<KpiResumenMensualDTO>>, t: Throwable) {
                    Log.e(TAG, "onFailure ejecutado: KPIs", t)
                    binding.pbLoadingDashboard.visibility = View.GONE
                    binding.layoutErrorDashboard.visibility = View.VISIBLE
                    binding.tvMensajeErrorDashboard.text = "Error de conexión al cargar indicadores"
                }
            })
    }

    private fun renderKPIs(kpi: KpiResumenMensualDTO) {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        binding.tvTotalIngresos.text = format.format(kpi.totalIngresos)
        binding.tvTotalGastos.text = format.format(kpi.totalEgresos)
        binding.tvSaldoReal.text = format.format(kpi.resultadoPeriodo)
        
        val color = if ((kpi.resultadoPeriodo ?: 0.0) >= 0) R.color.success else R.color.error
        binding.tvSaldoReal.setTextColor(getColor(color))

        // C9.6 - IVA
        binding.tvIvaDebito.text = format.format(kpi.ivaDebito ?: 0.0)
        binding.tvIvaCredito.text = format.format(kpi.ivaCredito ?: 0.0)
        val saldoIva = (kpi.ivaPorPagar ?: 0.0) - (kpi.remanenteIva ?: 0.0)
        binding.tvKpiSaldoIva.text = format.format(Math.abs(saldoIva))
        if (saldoIva > 0) {
            binding.tvLabelSaldoIva.text = "IVA POR PAGAR"
            binding.tvKpiSaldoIva.setTextColor(getColor(R.color.error))
        } else {
            binding.tvLabelSaldoIva.text = "REMANENTE IVA"
            binding.tvKpiSaldoIva.setTextColor(getColor(R.color.success))
        }
    }

    private fun limpiarKPIs() {
        binding.tvTotalIngresos.text = "$0"
        binding.tvTotalGastos.text = "$0"
        binding.tvSaldoReal.text = "$0"
        binding.tvIvaDebito.text = "$0"
        binding.tvIvaCredito.text = "$0"
        binding.tvKpiSaldoIva.text = "$0"
    }

    private fun fetchUltimosMovimientos(empresaId: String) {
        val filters = mapOf("empresa_id" to "eq.$empresaId")
        Log.d(TAG, "API llamada: fetchUltimosMovimientos")
        RetrofitClient.getApi(this).getVistaMovimientos(filters, range = "0-9")
            .enqueue(object : Callback<List<MovimientoDTO>> {
                override fun onResponse(call: Call<List<MovimientoDTO>>, response: Response<List<MovimientoDTO>>) {
                    Log.d(TAG, "onResponse ejecutado: Movimientos")
                    if (response.isSuccessful) {
                        binding.rvMovimientos.adapter = MovimientosAdapter(response.body() ?: emptyList())
                    }
                }

                override fun onFailure(call: Call<List<MovimientoDTO>>, t: Throwable) {
                    Log.e(TAG, "onFailure ejecutado: Movimientos", t)
                }
            })
    }

    private fun forceLogout() {
        getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    private fun redirigirASeleccion() {
        startActivity(Intent(this, EmpresasActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }
}
