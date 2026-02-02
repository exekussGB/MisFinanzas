package com.analistainacap.misfinanzas

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CrearEmpresaActivity : AppCompatActivity() {

    private val KEY_EMPRESA_ID = "empresa_id_activa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_empresa)

        val btnCrear = findViewById<Button>(R.id.btnCrearEmpresa)
        val btnVolver = findViewById<Button>(R.id.btnVolverEmpresas)

        btnCrear.setOnClickListener {
            ejecutarCreacion()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun ejecutarCreacion() {
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

        if (razonSocial.isEmpty() || rut.isEmpty() || giro.isEmpty()) {
            Toast.makeText(this, "Completa Razón Social, RUT y Giro", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateEmpresaRequest(
            razonSocial = razonSocial,
            rutEmpresa = rut,
            giro = giro,
            tipoEmpresa = tipo,
            fechaInicioActividades = if (fecha.isEmpty()) "2000-01-01" else fecha,
            direccionComercial = direccion,
            comuna = comuna,
            region = region,
            correoContacto = correo,
            telefonoContacto = telefono,
            representanteLegal = representante,
            regimenTributario = regimen,
            afectaIva = afectaIva
        )

        RetrofitClient.getApi(this).crearEmpresaRpc(request)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful && response.body() != null) {
                        val nuevoId = response.body()!!
                        saveEmpresaId(nuevoId, razonSocial)
                        Toast.makeText(this@CrearEmpresaActivity, "Empresa creada correctamente", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CrearEmpresaActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@CrearEmpresaActivity, "Error ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
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
