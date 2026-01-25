package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityMainBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val empresaId = prefs.getString("empresa_id", "") ?: ""
        val rol = prefs.getString("empresa_rol", "user") ?: "user"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUIByRol(rol)

        if (empresaId.isNotEmpty()) {
            fetchDashboard(empresaId)
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

    private fun showInvitacionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invitar Usuario")
        
        val input = EditText(this)
        input.hint = "Correo electrónico"
        builder.setView(input)

        builder.setPositiveButton("Invitar") { _, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                // Por defecto invitamos como 'lector' (o 'user')
                invitarUsuario(email, "lector")
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun invitarUsuario(email: String, rolInvitado: String) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val empresaId = prefs.getString("empresa_id", null) ?: return

        val request = InvitacionRequest(
            email = email,
            empresa_id = empresaId,
            rol = rolInvitado
        )

        RetrofitClient.getApi(this).crearInvitacion(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Invitación enviada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "No tienes permisos o error en datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchDashboard(id: String) {
        RetrofitClient.getApi(this).getDashboard("eq.$id")
            .enqueue(object : Callback<List<DashboardDTO>> {
                override fun onResponse(call: Call<List<DashboardDTO>>, response: Response<List<DashboardDTO>>) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        val d = response.body()!![0]
                        binding.tvStatus.text = "Empresa: ${d.nombre}\n\nSaldo Real:\n$${d.saldoReal}"
                    } else if (response.code() == 401) {
                        forceLogout()
                    } else {
                        binding.tvStatus.text = "Error al cargar datos"
                    }
                }

                override fun onFailure(call: Call<List<DashboardDTO>>, t: Throwable) {
                    binding.tvStatus.text = "Sin conexión"
                }
            })
    }

    private fun forceLogout() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
