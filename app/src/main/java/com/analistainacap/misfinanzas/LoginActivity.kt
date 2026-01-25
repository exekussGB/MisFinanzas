package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailIn = findViewById<EditText>(R.id.etEmail)
        val passIn = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        loginBtn.setOnClickListener {
            val email = emailIn.text.toString().trim()
            val pass = passIn.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Campos requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.isEnabled = false
            loginBtn.text = "Ingresando..."

            RetrofitClient.getApi(this).login(LoginRequest(email, pass))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        loginBtn.isEnabled = true
                        loginBtn.text = "Ingresar"

                        if (response.isSuccessful && response.body() != null) {
                            saveSession(response.body()!!.access_token)
                            startActivity(Intent(this@LoginActivity, EmpresasActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Acceso denegado", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        loginBtn.isEnabled = true
                        loginBtn.text = "Ingresar"
                        Toast.makeText(this@LoginActivity, "Error de red", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun saveSession(token: String) {
        getSharedPreferences("auth", MODE_PRIVATE).edit().putString("token", token).apply()
    }
}
