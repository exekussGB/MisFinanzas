package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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

        // Vincular con código
        binding.btnTengoCodigo.setOnClickListener {
            startActivity(Intent(this, AceptarInvitacionActivity::class.java))
        }

        // Crear nueva empresa
        binding.btnCrearEmpresa.setOnClickListener {
            startActivity(Intent(this, CrearEmpresaActivity::class.java))
        }

        // Botón Volver (Cerrar Sesión)
        binding.btnVolverLogin.setOnClickListener {
            forceLogout()
        }
    }

    private fun loadEmpresas() {
        binding.pbLoading.visibility = View.VISIBLE

        RetrofitClient.getApi(this).getEmpresasConRol().enqueue(object : Callback<List<EmpresaRolDTO>> {
            override fun onResponse(
                call: Call<List<EmpresaRolDTO>>,
                response: Response<List<EmpresaRolDTO>>
            ) {
                binding.pbLoading.visibility = View.GONE
                if (!response.isSuccessful) {
                    forceLogout()
                    return
                }

                val empresas = response.body() ?: return
                renderEmpresas(empresas)
            }

            override fun onFailure(call: Call<List<EmpresaRolDTO>>, t: Throwable) {
                binding.pbLoading.visibility = View.GONE
                showError("Sin conexión a internet")
            }
        })
    }

    private fun renderEmpresas(empresas: List<EmpresaRolDTO>) {
        binding.containerEmpresas.removeAllViews()

        if (empresas.isEmpty()) {
            showError("No se encontraron empresas")
            return
        }

        empresas.forEach { empresa ->
            val button = Button(this).apply {
                text = "${empresa.empresa_nombre}\n(${empresa.rol})"
                isAllCaps = false
                // Aumentar el tamaño del botón
                minHeight = dpToPx(80)
                textSize = 18f
                
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(8), 0, dpToPx(8))
                }

                setOnClickListener {
                    saveEmpresaContext(empresa)
                    openDashboard()
                }
            }
            binding.containerEmpresas.addView(button)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun saveEmpresaContext(empresa: EmpresaRolDTO) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit()
            .putString("empresa_id", empresa.empresa_id)
            .putString("empresa_rol", empresa.rol)
            .apply()
    }

    private fun openDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
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
