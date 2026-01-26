package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityEmpresasBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmpresasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        loadEmpresas()

        // Botón "Registrar Nueva Empresa"
        binding.btnCrearEmpresa.setOnClickListener {
            val intent = Intent(this, EmpresaFormActivity::class.java)
            startActivityForResult(intent, 100)
        }

        // Otros botones existentes
        binding.btnTengoCodigo.setOnClickListener {
            startActivity(Intent(this, AceptarInvitacionActivity::class.java))
        }

        binding.btnVolverLogin.setOnClickListener {
            forceLogout()
        }
    }

    private fun loadEmpresas() {
        binding.pbLoading.visibility = View.VISIBLE
        binding.containerEmpresas.removeAllViews()

        // Consumimos la vista segura de empresas del usuario
        RetrofitClient.getApi(this).getEmpresas().enqueue(object : Callback<List<EmpresaDTO>> {
            override fun onResponse(
                call: Call<List<EmpresaDTO>>,
                response: Response<List<EmpresaDTO>>
            ) {
                binding.pbLoading.visibility = View.GONE
                if (response.isSuccessful) {
                    val empresas = response.body() ?: emptyList()
                    renderEmpresas(empresas)
                } else if (response.code() == 401) {
                    forceLogout()
                } else {
                    showError("Error al cargar lista: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<EmpresaDTO>>, t: Throwable) {
                binding.pbLoading.visibility = View.GONE
                showError("Sin conexión a internet")
            }
        })
    }

    private fun renderEmpresas(empresas: List<EmpresaDTO>) {
        if (empresas.isEmpty()) {
            Toast.makeText(this, "No tienes empresas vinculadas", Toast.LENGTH_SHORT).show()
            return
        }

        empresas.forEach { empresa ->
            val button = Button(this).apply {
                text = "${empresa.razonSocial}\n(RUT: ${empresa.rutEmpresa})"
                isAllCaps = false
                minHeight = (80 * resources.displayMetrics.density).toInt()
                textSize = 18f
                
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 8, 0, 8)
                layoutParams = params

                setOnClickListener {
                    // Al pulsar, vamos al DETALLE para ver opciones de Dashboard, Editar o Borrar
                    val intent = Intent(this@EmpresasActivity, EmpresaDetalleActivity::class.java)
                    intent.putExtra("EXTRA_EMPRESA", empresa)
                    startActivity(intent)
                }
            }
            binding.containerEmpresas.addView(button)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            loadEmpresas() // Recargar lista si hubo cambios
        }
    }

    private fun forceLogout() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
