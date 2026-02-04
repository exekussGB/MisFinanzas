package com.analistainacap.misfinanzas

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
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
 * Implementa Auditoría de Cierre/Reapertura (C11.12) y Estados de Bloqueo.
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

        // Inicializar con período actual
        val cal = Calendar.getInstance()
        anioSel = cal.get(Calendar.YEAR)
        mesSel = cal.get(Calendar.MONTH) + 1
        
        actualizarPeriodoUI()
        consultarEstado()

        binding.tvPeriodoCierre.setOnClickListener { showPicker() }
        binding.btnVolverCierre.setOnClickListener { finish() }
        binding.btnValidarYEjecutarCierre.setOnClickListener { ejecutarProcesoCierre() }
        binding.btnReabrirPeriodo.setOnClickListener { solicitarReapertura() }
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
        val filters = mapOf(
            "empresa_id" to "eq.$empresaIdActiva",
            "mes" to "eq.$mesSel",
            "anio" to "eq.$anioSel"
        )
        RetrofitClient.getApi(this).getEstadoCierre(filters)
            .enqueue(object : Callback<List<CierreMensualDTO>> {
                override fun onResponse(call: Call<List<CierreMensualDTO>>, response: Response<List<CierreMensualDTO>>) {
                    binding.pbCierre.visibility = View.GONE
                    val cierre = response.body()?.firstOrNull()
                    updateUIEstado(cierre?.cerrado ?: false)
                }
                override fun onFailure(call: Call<List<CierreMensualDTO>>, t: Throwable) {
                    binding.pbCierre.visibility = View.GONE
                }
            })
    }

    private fun updateUIEstado(estaCerrado: Boolean) {
        val session = SessionManager(this)
        if (estaCerrado) {
            binding.tvValorEstado.text = "CERRADO"
            binding.tvValorEstado.setTextColor(getColor(R.color.error))
            binding.tvInfoCierre.text = "Período bloqueado. Solo lectura habilitada."
            binding.btnValidarYEjecutarCierre.visibility = View.GONE
            
            // C11.11 - Solo Owner puede reabrir
            binding.btnReabrirPeriodo.visibility = if (session.getRol() == SessionManager.ROLE_OWNER) View.VISIBLE else View.GONE
        } else {
            binding.tvValorEstado.text = "ABIERTO"
            binding.tvValorEstado.setTextColor(getColor(R.color.success))
            binding.tvInfoCierre.text = "Período disponible para edición."
            binding.btnValidarYEjecutarCierre.visibility = View.VISIBLE
            binding.btnReabrirPeriodo.visibility = View.GONE
        }
    }

    private fun ejecutarProcesoCierre() {
        // C11.12 - Proceso de Cierre con Registro RPC
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Cierre")
        builder.setMessage("¿Desea cerrar el período $anioSel-$mesSel? Esta acción bloqueará ediciones.")
        builder.setPositiveButton("Cerrar") { _, _ ->
            val params = mapOf(
                "p_empresa_id" to empresaIdActiva,
                "p_mes" to mesSel,
                "p_anio" to anioSel
            )
            RetrofitClient.getApi(this).ejecutarCierre(params).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CierreMensualActivity, "Período cerrado exitosamente", Toast.LENGTH_SHORT).show()
                        consultarEstado()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun solicitarReapertura() {
        // C11.11 y C11.12 - Reapertura con Motivo RPC
        val input = EditText(this)
        input.hint = "Indique motivo de reapertura"
        
        AlertDialog.Builder(this)
            .setTitle("Reabrir Período")
            .setView(input)
            .setPositiveButton("Reabrir") { _, _ ->
                val motivo = input.text.toString().trim()
                if (motivo.isEmpty()) {
                    Toast.makeText(this, "Motivo obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                ejecutarReapertura(motivo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarReapertura(motivo: String) {
        val params = mapOf(
            "p_empresa_id" to empresaIdActiva,
            "p_mes" to mesSel,
            "p_anio" to anioSel,
            "p_motivo" to motivo
        )
        RetrofitClient.getApi(this).reabrirPeriodo(params).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CierreMensualActivity, "Período reabierto correctamente", Toast.LENGTH_SHORT).show()
                    consultarEstado()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }
}
