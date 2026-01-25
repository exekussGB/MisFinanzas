package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Actividad para registrar una nueva empresa en el sistema.
 * Consume la función RPC 'crear_empresa' de Supabase.
 */
class CrearEmpresaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_empresa)

        val btnCrear = findViewById<Button>(R.id.btnCrearEmpresa)

        btnCrear.setOnClickListener {
            ejecutarCreacion()
        }
    }

    private fun ejecutarCreacion() {
        // --- 1. Captura de datos desde los inputs ---
        val razonSocial = findViewById<EditText>(R.id.etRazonSocial).text.toString().trim()
        val rut = findViewById<EditText>(R.id.etRut).text.toString().trim()
        val giro = findViewById<EditText>(R.id.etGiro).text.toString().trim()
        val tipo = findViewById<EditText>(R.id.etTipoEmpresa).text.toString().trim()
        val fecha = findViewById<EditText>(R.id.etFechaInicio).text.toString().trim()
        val direccion = findViewById<EditText>(R.id.etDireccion).text.toString().trim()
        val comuna = findViewById<EditText>(R.id.etComuna).text.toString().trim()
        val region = findViewById<EditText>(R.id.etRegion).text.toString().trim()
        val correo = findViewById<EditText>(R.id.etCorreo).text.toString().trim()
        val telefono = findViewById<EditText>(R.id.etTelefono).text.toString().trim()
        val representante = findViewById<EditText>(R.id.etRepresentante).text.toString().trim()
        val regimen = findViewById<EditText>(R.id.etRegimen).text.toString().trim()
        val afectaIva = findViewById<CheckBox>(R.id.cbAfectaIva).isChecked

        // --- 2. Validaciones Mínimas ---
        if (razonSocial.isEmpty() || rut.isEmpty() || giro.isEmpty()) {
            Toast.makeText(this, "Completa Razón Social, RUT y Giro", Toast.LENGTH_SHORT).show()
            return
        }

        // --- 3. Preparación del Request ---
        val request = CreateEmpresaRequest(
            razon_social = razonSocial,
            rut_empresa = rut,
            giro = giro,
            tipo_empresa = tipo,
            fecha_inicio_actividades = if (fecha.isEmpty()) "2000-01-01" else fecha, // Default por seguridad
            direccion = direccion,
            comuna = comuna,
            region = region,
            correo = correo,
            telefono = telefono,
            representante_legal = representante,
            regimen_tributario = regimen,
            afecta_iva = afectaIva
        )

        // --- 4. Llamada al endpoint vía Retrofit Enqueue ---
        RetrofitClient.getApi(this).crearEmpresa(request)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful && response.body() != null) {
                        val nuevoId = response.body()!!
                        
                        // Guardar ID en preferencias
                        saveEmpresaId(nuevoId)
                        
                        Toast.makeText(this@CrearEmpresaActivity, "Empresa creada correctamente", Toast.LENGTH_SHORT).show()
                        
                        // Navegar al Dashboard
                        val intent = Intent(this@CrearEmpresaActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@CrearEmpresaActivity, "Error al crear: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@CrearEmpresaActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveEmpresaId(id: String) {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putString("empresa_id", id)
            .apply()
    }
}
