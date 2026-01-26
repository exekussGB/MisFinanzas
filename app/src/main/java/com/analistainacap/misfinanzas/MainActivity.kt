package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.analistainacap.misfinanzas.databinding.ActivityMainBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val empresaId = prefs.getString("empresa_id", "") ?: ""
        val empresaNombre = prefs.getString("empresa_nombre", "Mi Empresa") ?: "Mi Empresa"
        val rol = prefs.getString("empresa_rol", "user") ?: "user"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la cabecera
        binding.tvEmpresaNombre.text = empresaNombre
        setupUIByRol(rol)

        // Configuración del RecyclerView para movimientos
        binding.rvMovimientos.layoutManager = LinearLayoutManager(this)

        if (empresaId.isNotEmpty()) {
            fetchDashboard(empresaId)
            fetchMovimientos(empresaId)
        } else {
            forceLogout()
        }

        binding.btnLogout.setOnClickListener { forceLogout() }
        
        binding.btnInvitarUsuario.setOnClickListener {
            showInvitacionDialog()
        }
    }

    private fun setupUIByRol(rol: String) {
        if (rol == "admin" || rol == "owner") {
            binding.btnInvitarUsuario.visibility = View.VISIBLE
        } else {
            binding.btnInvitarUsuario.visibility = View.GONE
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        return format.format(amount)
    }

    private fun fetchDashboard(id: String) {
        RetrofitClient.getApi(this).getDashboard("eq.$id")
            .enqueue(object : Callback<List<DashboardDTO>> {
                override fun onResponse(call: Call<List<DashboardDTO>>, response: Response<List<DashboardDTO>>) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        val d = response.body()!![0]
                        binding.tvSaldoReal.text = formatCurrency(d.saldoReal)
                        binding.tvTotalIngresos.text = formatCurrency(d.ingresos)
                        binding.tvTotalGastos.text = formatCurrency(d.gastos)
                    } else if (response.code() == 401) {
                        forceLogout()
                    }
                }

                override fun onFailure(call: Call<List<DashboardDTO>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error al cargar dashboard", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchMovimientos(id: String) {
        RetrofitClient.getApi(this).getMovimientos("eq.$id")
            .enqueue(object : Callback<List<MovimientoDTO>> {
                override fun onResponse(call: Call<List<MovimientoDTO>>, response: Response<List<MovimientoDTO>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val movimientos = response.body()!!
                        binding.rvMovimientos.adapter = MovimientosAdapter(movimientos)
                    }
                }

                override fun onFailure(call: Call<List<MovimientoDTO>>, t: Throwable) {
                    // Manejo silencioso o log
                }
            })
    }

    private fun showInvitacionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invitar Usuario")
        val input = EditText(this)
        input.hint = "Correo electrónico"
        builder.setView(input)
        builder.setPositiveButton("Invitar") { _, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) invitarUsuario(email, "lector")
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun invitarUsuario(email: String, rolInvitado: String) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val empresaId = prefs.getString("empresa_id", null) ?: return

        val request = InvitacionRequest(email, empresaId, rolInvitado)
        RetrofitClient.getApi(this).crearInvitacion(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) Toast.makeText(this@MainActivity, "Invitación enviada", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun forceLogout() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
}
