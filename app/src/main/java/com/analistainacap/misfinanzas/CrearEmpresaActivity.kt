package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CrearEmpresaActivity : AppCompatActivity() {

    private val KEY_EMPRESA_ID = "empresa_id_activa"
    private lateinit var spAfectaIva: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_empresa)

        spAfectaIva = findViewById(R.id.spAfectaIva)
        val btnCrear = findViewById<Button>(R.id.btnCrearEmpresa)
        val btnVolver = findViewById<Button>(R.id.btnVolverEmpresas)

        val adapterSiNo = ArrayAdapter.createFromResource(
            this,
            R.array.opciones_si_no,
            android.R.layout.simple_spinner_item
        )
        adapterSiNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAfectaIva.adapter = adapterSiNo

        btnCrear.setOnClickListener {
            ejecutarCreacion()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun ejecutarCreacion() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val userId = prefs.getString("user_id", "") ?: ""

        if (token == null) {
            Log.e("AuthCheck", "ERROR: No hay Token JWT. Supabase te rechazará.")
        }

        val razonSocial = findViewById<EditText>(R.id.etRazonSocial).text.toString().trim()
        val rut = findViewById<EditText>(R.id.etRut).text.toString().trim()
        val giro = findViewById<EditText>(R.id.etGiro).text.toString().trim()
        val tipo = findViewById<EditText>(R.id.etTipoEmpresa).text.toString().trim()
        val fecha = findViewById<EditText>(R.id.etFechaInicio).text.toString().trim()
        val direccion = findViewById<EditText>(R.id.etDireccion).text.toString().trim()
        val correo = findViewById<EditText>(R.id.etCorreo).text.toString().trim()
        val telefono = findViewById<EditText>(R.id.etTelefono).text.toString().trim()
        val representante = findViewById<EditText>(R.id.etRepresentante).text.toString().trim()
        val regimen = findViewById<EditText>(R.id.etRegimen).text.toString().trim()
        
        val afectaIvaSeleccion = when (spAfectaIva.selectedItem.toString()) {
            "Sí" -> true
            "No" -> false
            else -> false
        }

        if (razonSocial.isEmpty() || rut.isEmpty() || giro.isEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "Completa Razón Social, RUT, Giro y asegúrate de estar logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateEmpresaRequest(
            razon_social = razonSocial,
            rut_empresa = rut,
            giro = giro.ifEmpty { null },
            tipo_empresa = tipo.ifEmpty { null },
            direccion_comercial = direccion.ifEmpty { null },
            correo_contacto = correo.ifEmpty { null },
            telefono_contacto = telefono.ifEmpty { null }
        )

        Log.d("SupabaseDebug", "PAYLOAD: ${com.google.gson.Gson().toJson(request)}")

        RetrofitClient.getApi(this).crearEmpresaRpc(request)
            .enqueue(object : Callback<UUIDResponse> {
                override fun onResponse(call: Call<UUIDResponse>, response: Response<UUIDResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        // Capturamos el UUID generado (Paso Crítico)
                        val nuevoId = response.body()!!.id
                        saveEmpresaId(nuevoId, razonSocial)
                        
                        Toast.makeText(this@CrearEmpresaActivity, "Empresa creada correctamente", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CrearEmpresaActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("SUPABASE_ERROR", "HTTP ${response.code()}: $errorBody")
                        Toast.makeText(this@CrearEmpresaActivity, "Error ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<UUIDResponse>, t: Throwable) {
                    Toast.makeText(this@CrearEmpresaActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveEmpresaId(id: String, nombre: String) {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putString(KEY_EMPRESA_ID, id)
            .putString("empresa_nombre", nombre)
            .apply()
    }
}
