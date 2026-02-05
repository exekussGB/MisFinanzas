package com.analistainacap.misfinanzas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityEmpresaDetalleBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmpresaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresaDetalleBinding
    private lateinit var sessionManager: SessionManager
    private var empresa: EmpresaDTO? = null
    private val KEY_EMPRESA_ID_PREFS = "empresa_id_activa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // A0.1 — Confirmación técnica de carga de la Activity
        Log.e("DEBUG_ACTIVITY", "EmpresaDetailActivity CARGADA")

        sessionManager = SessionManager(this)
        empresa = intent.getSerializableExtra("EXTRA_EMPRESA") as? EmpresaDTO

        if (empresa == null) {
            finish()
            return
        }

        mostrarDatos()
        setupPermissionsUI()

        binding.btnVerDashboard.setOnClickListener {
            if (empresa?.id != null) {
                val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString(KEY_EMPRESA_ID_PREFS, empresa?.id)
                    .putString("empresa_nombre", empresa?.razonSocial)
                    .apply()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("empresa_id", empresa?.id)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error: ID de empresa no encontrado", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditar.setOnClickListener {
            val intent = Intent(this, EmpresaFormActivity::class.java)
            intent.putExtra("EXTRA_EMPRESA", empresa)
            startActivity(intent)
        }

        binding.btnEliminar.setOnClickListener {
            eliminarLogico()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun setupPermissionsUI() {
        // Verificación rápida (Paso A)
        Log.d("EMPRESA_ROLE", "Empresa ${empresa?.id} rol=${empresa?.rol}")

        // --- Log de depuración de ROL (Nueva prueba) ---
        Log.e("DEBUG_ROL", "ROL = ${empresa?.rol}")

        // --- Habilitar edición según rol (Paso A) ---
        if (empresa?.rol?.uppercase() == "OWNER") {
            binding.btnEditar.isEnabled = true
            binding.btnEditar.visibility = View.VISIBLE
        } else {
            binding.btnEditar.isEnabled = false
            binding.btnEditar.visibility = View.GONE
        }

        // Bloque B3 original para eliminar (solo owner puede borrar)
        binding.btnEliminar.visibility = if (sessionManager.puedeEliminarEmpresa()) View.VISIBLE else View.GONE
    }

    private fun mostrarDatos() {
        binding.tvRazonSocial.text = empresa?.razonSocial ?: "N/A"
        binding.tvRut.text = "RUT: ${empresa?.rutEmpresa ?: "N/A"}"
        
        val detalles = StringBuilder()
        detalles.append("Giro: ${empresa?.giro ?: "N/A"}\n\n")
        detalles.append("Tipo: ${empresa?.tipoEmpresa ?: "N/A"}\n\n")
        detalles.append("Inicio Actividades: ${empresa?.fechaInicioActividades ?: "N/A"}\n\n")
        detalles.append("Dirección: ${empresa?.direccionComercial ?: "N/A"}\n\n")
        detalles.append("Correo: ${empresa?.correoContacto ?: "N/A"}\n\n")
        detalles.append("Teléfono: ${empresa?.telefonoContacto ?: "N/A"}\n\n")
        detalles.append("Estado: ${empresa?.estadoEmpresa ?: "N/A"}")

        binding.tvDetalles.text = detalles.toString()
    }

    /**
     * ✅ FUNCIÓN eliminarLogico() (CORREGIDA Y COMPILABLE)
     * BLOQUE 3 — Implementación definitiva del PATCH para actualización de estado.
     */
    private fun eliminarLogico() {
        val e = empresa ?: return
        val id = e.id ?: return

        val request = UpdateEmpresaRequest(
            razon_social = e.razonSocial ?: return,
            rut_empresa = e.rutEmpresa ?: return,
            giro = e.giro,
            tipo_empresa = e.tipoEmpresa,
            direccion_comercial = e.direccionComercial,
            correo_contacto = e.correoContacto,
            telefono_contacto = e.telefonoContacto
        )

        RetrofitClient.getApi(this)
            .editarEmpresa("eq.$id", request)
            .enqueue(object : Callback<Void> {

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EmpresaDetalleActivity,
                            "Empresa actualizada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EmpresaDetalleActivity,
                            "Error al editar empresa (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@EmpresaDetalleActivity,
                        "Error de red",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
