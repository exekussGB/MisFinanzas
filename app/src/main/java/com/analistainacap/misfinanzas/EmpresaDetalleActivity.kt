package com.analistainacap.misfinanzas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityEmpresaDetalleBinding
import com.analistainacap.misfinanzas.network.EmpresaDTO
import com.analistainacap.misfinanzas.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmpresaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresaDetalleBinding
    private var empresa: EmpresaDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        empresa = intent.getSerializableExtra("EXTRA_EMPRESA") as? EmpresaDTO

        if (empresa == null) {
            finish()
            return
        }

        mostrarDatos()

        // IR AL DASHBOARD FINANCIERO
        binding.btnVerDashboard.setOnClickListener {
            // Guardamos el contexto de la empresa actual para que el dashboard sepa cuál mostrar
            val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("empresa_id", empresa?.id)
                .putString("empresa_nombre", empresa?.razonSocial)
                .apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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

    private fun mostrarDatos() {
        binding.tvRazonSocial.text = empresa?.razonSocial
        binding.tvRut.text = "RUT: ${empresa?.rutEmpresa}"
        
        val detalles = StringBuilder()
        detalles.append("Giro: ${empresa?.giro}\n\n")
        detalles.append("Tipo: ${empresa?.tipoEmpresa ?: "N/A"}\n\n")
        detalles.append("Inicio Actividades: ${empresa?.fechaInicio ?: "N/A"}\n\n")
        detalles.append("Dirección: ${empresa?.direccionComercial ?: "N/A"}\n\n")
        detalles.append("Comuna: ${empresa?.comuna ?: "N/A"}\n\n")
        detalles.append("Región: ${empresa?.region ?: "N/A"}\n\n")
        detalles.append("Correo: ${empresa?.correoContacto ?: "N/A"}\n\n")
        detalles.append("Teléfono: ${empresa?.telefonoContacto ?: "N/A"}\n\n")
        detalles.append("Estado: ${empresa?.estadoEmpresa}")

        binding.tvDetalles.text = detalles.toString()
    }

    private fun eliminarLogico() {
        if (empresa?.id == null) return

        val update = empresa!!.copy(activa = false, estadoEmpresa = "eliminada")
        
        RetrofitClient.getApi(this).editarEmpresa("eq.${empresa!!.id}", update)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EmpresaDetalleActivity, "Empresa eliminada", Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (response.code() == 403) {
                        Toast.makeText(this@EmpresaDetalleActivity, "No tienes permisos (Owner requerido)", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@EmpresaDetalleActivity, "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
