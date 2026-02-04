package com.analistainacap.misfinanzas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityEmpresaFormBinding
import com.analistainacap.misfinanzas.network.CreateEmpresaRequest
import com.analistainacap.misfinanzas.network.EmpresaDTO
import com.analistainacap.misfinanzas.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmpresaFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresaFormBinding
    private var empresaExistente: EmpresaDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Corrección de Binding: Usar el layout del formulario
        binding = ActivityEmpresaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        empresaExistente = intent.getSerializableExtra("EXTRA_EMPRESA") as? EmpresaDTO

        if (empresaExistente != null) {
            binding.tvFormTitle.text = "Editar Empresa"
            binding.btnGuardar.text = "Actualizar Cambios"
            rellenarFormulario(empresaExistente!!)
        } else {
            binding.tvFormTitle.text = "Nueva Empresa"
            binding.btnGuardar.text = "Crear Empresa"
        }

        binding.btnGuardar.setOnClickListener {
            validarYGuardar()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun rellenarFormulario(e: EmpresaDTO) {
        binding.etRazonSocial.setText(e.razonSocial)
        binding.etRut.setText(e.rutEmpresa)
        binding.etGiro.setText(e.giro)
        binding.etTipoEmpresa.setText(e.tipoEmpresa)
        // Sincronizado con Models.kt
        binding.etFechaInicio.setText(e.fechaInicioActividades)
        binding.etDireccion.setText(e.direccionComercial)
        binding.etCorreo.setText(e.correoContacto)
        binding.etTelefono.setText(e.telefonoContacto)
    }

    private fun validarYGuardar() {
        val razon = binding.etRazonSocial.text.toString().trim()
        val rut = binding.etRut.text.toString().trim()
        val giro = binding.etGiro.text.toString().trim()

        if (razon.isEmpty() || rut.isEmpty() || giro.isEmpty()) {
            Toast.makeText(this, "Razón Social, RUT y Giro son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.getApi(this)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getString("user_id", "") ?: ""
        
        val call: Call<List<EmpresaDTO>> = if (empresaExistente == null) {
            // Flujo Creación: Crear Request con ownerId obligatorio
            val request = CreateEmpresaRequest(
                nombre = razon,
                razonSocial = razon,
                rutEmpresa = rut,
                ownerId = userId,
                giro = giro,
                tipoEmpresa = binding.etTipoEmpresa.text.toString().trim(),
                fechaInicioActividades = binding.etFechaInicio.text.toString().trim(),
                direccionComercial = binding.etDireccion.text.toString().trim(),
                correoContacto = binding.etCorreo.text.toString().trim(),
                telefonoContacto = binding.etTelefono.text.toString().trim()
            )
            api.crearEmpresa(request)
        } else {
            // Flujo Edición: Filtro Map y Body EmpresaDTO
            val filters = mapOf("id" to "eq.${empresaExistente!!.id}")
            val empresaUpdate = EmpresaDTO(
                id = empresaExistente!!.id,
                razonSocial = razon,
                rutEmpresa = rut,
                giro = giro,
                tipoEmpresa = binding.etTipoEmpresa.text.toString().trim(),
                fechaInicioActividades = binding.etFechaInicio.text.toString().trim(),
                direccionComercial = binding.etDireccion.text.toString().trim(),
                correoContacto = binding.etCorreo.text.toString().trim(),
                telefonoContacto = binding.etTelefono.text.toString().trim(),
                estadoEmpresa = empresaExistente!!.estadoEmpresa,
                rol = empresaExistente!!.rol,
                activa = empresaExistente!!.activa
            )
            api.editarEmpresa(filters, empresaUpdate)
        }

        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "Procesando..."

        call.enqueue(object : Callback<List<EmpresaDTO>> {
            override fun onResponse(call: Call<List<EmpresaDTO>>, response: Response<List<EmpresaDTO>>) {
                binding.btnGuardar.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@EmpresaFormActivity, "Empresa guardada", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EmpresaFormActivity, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                    binding.btnGuardar.text = "Reintentar"
                }
            }

            override fun onFailure(call: Call<List<EmpresaDTO>>, t: Throwable) {
                binding.btnGuardar.isEnabled = true
                binding.btnGuardar.text = "Guardar Empresa"
                Toast.makeText(this@EmpresaFormActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
