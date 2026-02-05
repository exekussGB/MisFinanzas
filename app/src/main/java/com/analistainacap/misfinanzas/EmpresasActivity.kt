package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityEmpresasBinding
import com.analistainacap.misfinanzas.network.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Listado de Empresas (C1).
 */
class EmpresasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        loadEmpresas()

        binding.btnCrearEmpresa.setOnClickListener {
            Log.d("BTN_CREAR_EMPRESA", "Click crear empresa - Iniciando Formulario")
            val intent = Intent(this, EmpresaFormActivity::class.java)
            startActivityForResult(intent, 100)
        }

        binding.btnTengoCodigo.setOnClickListener {
            startActivity(Intent(this, AceptarInvitacionActivity::class.java))
        }

        binding.btnVolverLogin.setOnClickListener {
            forceLogout()
        }
    }

    private fun loading(show: Boolean) {
        binding.pbLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun loadEmpresas() {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", null)
        if (token == null) {
            forceLogout()
            return
        }

        loading(true)
        binding.containerEmpresas.removeAllViews()

        RetrofitClient.getApi(this).getEmpresas().enqueue(object : Callback<List<EmpresaDTO>> {
            override fun onResponse(call: Call<List<EmpresaDTO>>, response: Response<List<EmpresaDTO>>) {
                loading(false)

                if (!response.isSuccessful) {
                    val errorDetail = response.errorBody()?.string() ?: "Error sin detalle"
                    Log.e("SupabaseSelect", "HTTP ${response.code()}: $errorDetail")
                    showError("Error al cargar la lista de empresas")
                    return
                }

                val empresas = response.body() ?: emptyList()
                
                // --- Sincronización de Rol (Bloque Crítico) ---
                empresas.forEach { empresa ->
                    // Extraemos el rol del JOIN anidado devuelto por PostgREST
                    val rolExtraido = empresa.empresaUsuarios?.firstOrNull()?.rol
                    empresa.rol = rolExtraido
                    Log.d("DEBUG_JSON", "Empresa: ${empresa.razonSocial}, Rol: $rolExtraido")
                }

                if (empresas.isEmpty()) {
                    Toast.makeText(this@EmpresasActivity, "No tienes empresas vinculadas", Toast.LENGTH_SHORT).show()
                    return
                }

                renderEmpresas(empresas)
            }

            override fun onFailure(call: Call<List<EmpresaDTO>>, t: Throwable) {
                loading(false)
                Log.e("NetworkFail", t.message ?: "Error de red")
                showError("Sin conexión a internet")
            }
        })
    }

    private fun renderEmpresas(empresas: List<EmpresaDTO>) {
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
            loadEmpresas()
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
