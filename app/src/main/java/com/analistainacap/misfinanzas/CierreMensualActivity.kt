package com.analistainacap.misfinanzas

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityCierreMensualBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Gestión de Cierre Mensual (C11).
 * Implementa BLOQUE B4-B: Flujo RPC sincronizado con Supabase.
 */
class CierreMensualActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCierreMensualBinding
    private val KEY_EMPRESA_ID = "empresa_id_activa"
    private var empresaIdActiva = ""
    private var mesSel = 0
    private var anioSel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCierreMensualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        empresaIdActiva = prefs.getString(KEY_EMPRESA_ID, "") ?: ""

        if (empresaIdActiva.isEmpty()) {
            finish()
            return
        }

        val cal = Calendar.getInstance()
        anioSel = cal.get(Calendar.YEAR)
        mesSel = cal.get(Calendar.MONTH) + 1
        
        actualizarPeriodoUI()
        consultarEstado()

        binding.tvPeriodoCierre.setOnClickListener { showPicker() }
        binding.btnVolverCierre.setOnClickListener { finish() }
        binding.btnValidarYEjecutarCierre.setOnClickListener { ejecutarProcesoCierre() }
        binding.btnReabrirPeriodo.setOnClickListener { ejecutarProcesoReapertura() }
    }

    private fun actualizarPeriodoUI() {
        binding.tvPeriodoCierre.text = String.format(Locale("es", "CL"), "%d-%02d", anioSel, mesSel)
    }

    private fun showPicker() {
        DatePickerDialog(this, { _, year, month, _ ->
            anioSel = year
            mesSel = month + 1
            actualizarPeriodoUI()
            consultarEstado()
        }, anioSel, mesSel - 1, 1).show()
    }

    private fun consultarEstado() {
        binding.pbCierre.visibility = View.VISIBLE
        val params = mapOf(
            "empresa_id" to empresaIdActiva,
            "anio" to anioSel.toString(),
            "mes" to mesSel.toString()
        )
        
        RetrofitClient.getApi(this).getEstadoCierre(params)
            .enqueue(object : Callback<List<CierreMensualDTO>> {
                override fun onResponse(call: Call<List<CierreMensualDTO>>, response: Response<List<CierreMensualDTO>>) {
                    binding.pbCierre.visibility = View.GONE
                    if (response.isSuccessful) {
                        val cierre = response.body()?.firstOrNull()
                        updateUIEstado(cierre?.cerrado ?: false)
                    } else {
                        Log.e("SupabaseError", "Error Estado: ${response.errorBody()?.string()}")
                    }
                }
                override fun onFailure(call: Call<List<CierreMensualDTO>>, t: Throwable) {
                    binding.pbCierre.visibility = View.GONE
                    Log.e("NetworkFail", t.message ?: "Error de conexión")
                }
            })
    }

    private fun updateUIEstado(estaCerrado: Boolean) {
        if (estaCerrado) {
            binding.tvValorEstado.text = "CERRADO"
            binding.tvValorEstado.setTextColor(getColor(R.color.error))
            binding.btnValidarYEjecutarCierre.visibility = View.GONE
            binding.btnReabrirPeriodo.visibility = View.VISIBLE
        } else {
            binding.tvValorEstado.text = "ABIERTO"
            binding.tvValorEstado.setTextColor(getColor(R.color.success))
            binding.btnValidarYEjecutarCierre.visibility = View.VISIBLE
            binding.btnReabrirPeriodo.visibility = View.GONE
        }
    }

    private fun ejecutarProcesoCierre() {
        val params = mapOf(
            "empresa_id" to empresaIdActiva,
            "anio" to anioSel.toString(),
            "mes" to mesSel.toString()
        )
        
        RetrofitClient.getApi(this).ejecutarCierre(params).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CierreMensualActivity, "Cierre exitoso", Toast.LENGTH_SHORT).show()
                    consultarEstado()
                } else {
                    Log.e("SupabaseError", "Error Cierre: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

    private fun ejecutarProcesoReapertura() {
        val params = mapOf(
            "empresa_id" to empresaIdActiva,
            "anio" to anioSel.toString(),
            "mes" to mesSel.toString()
        )
        
        RetrofitClient.getApi(this).reabrirPeriodo(params).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CierreMensualActivity, "Periodo reabierto", Toast.LENGTH_SHORT).show()
                    consultarEstado()
                } else {
                    Log.e("SupabaseError", "Error Reapertura: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }
}
