package com.analistainacap.misfinanzas

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.analistainacap.misfinanzas.databinding.ActivityAuditoriaBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Pantalla de Auditoría Contable (C7).
 * Muestra el registro de cambios (INSERT, UPDATE, DELETE) de la empresa.
 * Implementa Corrección Técnica Post-Refactor (Opción B: Limpieza).
 */
class AuditoriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuditoriaBinding
    private lateinit var sessionManager: SessionManager
    private var empresaId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuditoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        empresaId = sessionManager.getEmpresaId()

        // 1️⃣ Guardrail de Permisos: Basado en rol activo en empresa (Bloque B3)
        if (!sessionManager.puedeVerAuditoria()) {
            Toast.makeText(this, "Acceso denegado: Se requiere rol de Contador u Owner", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        loadLogs()
    }

    private fun setupUI() {
        binding.rvAuditoria.layoutManager = LinearLayoutManager(this)
        binding.btnVolverAuditoria.setOnClickListener { finish() }
    }

    private fun loadLogs() {
        binding.pbAuditoria.visibility = View.VISIBLE
        
        val filters = mutableMapOf("empresa_id" to "eq.$empresaId")

        RetrofitClient.getApi(this).getAuditoria(filters)
            .enqueue(object : Callback<List<AuditoriaDTO>> {
                override fun onResponse(call: Call<List<AuditoriaDTO>>, response: Response<List<AuditoriaDTO>>) {
                    binding.pbAuditoria.visibility = View.GONE
                    if (response.isSuccessful) {
                        val logs = response.body() ?: emptyList()
                        if (logs.isEmpty()) {
                            Toast.makeText(this@AuditoriaActivity, "No hay registros de auditoría", Toast.LENGTH_SHORT).show()
                        }
                        binding.rvAuditoria.adapter = AuditoriaAdapter(logs.toMutableList())
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("AuditoriaError", "HTTP ${response.code()}: $errorBody")
                    }
                }

                override fun onFailure(call: Call<List<AuditoriaDTO>>, t: Throwable) {
                    binding.pbAuditoria.visibility = View.GONE
                    Log.e("NetworkFail", t.message ?: "Fallo conexión")
                    Toast.makeText(this@AuditoriaActivity, "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
