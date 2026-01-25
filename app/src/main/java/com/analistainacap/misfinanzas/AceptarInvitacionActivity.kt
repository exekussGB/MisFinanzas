package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AceptarInvitacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aceptar_invitacion)

        val etToken = findViewById<EditText>(R.id.etToken)
        val btnAceptar = findViewById<Button>(R.id.btnAceptar)
        val btnVolver = findViewById<Button>(R.id.btnVolverEmpresas)

        btnAceptar.setOnClickListener {
            val token = etToken.text.toString().trim()
            if (token.length == 36) {
                aceptarInvitacion(token)
            } else {
                showToast("Código inválido (debe ser UUID)")
            }
        }

        btnVolver.setOnClickListener {
            finish() // Vuelve a la pantalla anterior (EmpresasActivity)
        }
    }

    private fun aceptarInvitacion(token: String) {
        val body = mapOf("p_token" to token)

        RetrofitClient.getApi(this).aceptarInvitacion(body).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Invitación aceptada")
                    goToEmpresas()
                } else {
                    showToast("Invitación inválida o ya aceptada")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Error de conexión")
            }
        })
    }

    private fun goToEmpresas() {
        val intent = Intent(this, EmpresasActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
