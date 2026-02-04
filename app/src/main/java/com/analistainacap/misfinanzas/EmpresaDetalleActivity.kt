package com.analistainacap.misfinanzas

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        // --- UI Defensiva (Bloque B3) ---
        binding.btnEditar.visibility = if (sessionManager.puedeGestionarEmpresa()) View.VISIBLE else View.GONE
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

    private fun eliminarLogico() {
        if (!sessionManager.puedeEliminarEmpresa()) return

        val updateMap = mapOf(
            "activa" to false,
            "estado_empresa" to "eliminada"
        )
        val filters = mapOf("id" to "eq.${empresa!!.id}")
        
        RetrofitClient.getApi(this).editarEmpresa(filters, updateMap)
            .enqueue(object : Callback<List<EmpresaDTO>> {
                override fun onResponse(call: Call<List<EmpresaDTO>>, response: Response<List<EmpresaDTO>>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EmpresaDetalleActivity, "Empresa eliminada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                override fun onFailure(call: Call<List<EmpresaDTO>>, t: Throwable) {
                    Toast.makeText(this@EmpresaDetalleActivity, "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
