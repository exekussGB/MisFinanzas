package com.analistainacap.misfinanzas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityEmpresaFormBinding
import com.analistainacap.misfinanzas.network.EmpresaDTO
import com.analistainacap.misfinanzas.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Actividad para crear o editar una empresa.
 * La lógica de permisos (403 Forbidden) es manejada directamente por el backend de Supabase.
 */
class EmpresaFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresaFormBinding
    private var empresaExistente: EmpresaDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intentamos obtener la empresa si venimos de "Editar"
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
        binding.etFechaInicio.setText(e.fechaInicio)
        binding.etDireccion.setText(e.direccionComercial)
        binding.etComuna.setText(e.comuna)
        binding.etRegion.setText(e.region)
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

        val empresa = EmpresaDTO(
            id = empresaExistente?.id,
            razonSocial = razon,
            rutEmpresa = rut,
            giro = giro,
            tipoEmpresa = binding.etTipoEmpresa.text.toString().trim(),
            fechaInicio = binding.etFechaInicio.text.toString().trim(),
            direccionComercial = binding.etDireccion.text.toString().trim(),
            comuna = binding.etComuna.text.toString().trim(),
            region = binding.etRegion.text.toString().trim(),
            correoContacto = binding.etCorreo.text.toString().trim(),
            telefonoContacto = binding.etTelefono.text.toString().trim()
        )

        val api = RetrofitClient.getApi(this)
        
        val call = if (empresaExistente == null) {
            api.crearEmpresa(empresa)
        } else {
            api.editarEmpresa("eq.${empresaExistente!!.id}", empresa)
        }

        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "Procesando..."

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                binding.btnGuardar.isEnabled = true
                
                if (response.isSuccessful) {
                    Toast.makeText(this@EmpresaFormActivity, "Empresa guardada", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    when (response.code()) {
                        403 -> Toast.makeText(this@EmpresaFormActivity, "Error 403: No tienes permisos para esta acción", Toast.LENGTH_LONG).show()
                        401 -> Toast.makeText(this@EmpresaFormActivity, "Sesión expirada", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@EmpresaFormActivity, "Error del servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                    binding.btnGuardar.text = "Reintentar"
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                binding.btnGuardar.isEnabled = true
                binding.btnGuardar.text = "Guardar Empresa"
                Toast.makeText(this@EmpresaFormActivity, "Fallo de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
