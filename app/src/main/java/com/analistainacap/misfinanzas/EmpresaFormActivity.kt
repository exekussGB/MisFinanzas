package com.analistainacap.misfinanzas

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityEmpresaFormBinding
import com.analistainacap.misfinanzas.network.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Actividad para crear o editar una empresa.
 */
class EmpresaFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresaFormBinding
    private lateinit var sessionManager: SessionManager
    private var empresaExistente: EmpresaDTO? = null
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupSpinners()

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

    private fun setupSpinners() {
        val adapterEstado = ArrayAdapter.createFromResource(this, R.array.estados_empresa, android.R.layout.simple_spinner_item)
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spEstado.adapter = adapterEstado

        val adapterIva = ArrayAdapter.createFromResource(this, R.array.opciones_si_no, android.R.layout.simple_spinner_item)
        adapterIva.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spAfectaIva.adapter = adapterIva

        val adapterRegimen = ArrayAdapter.createFromResource(this, R.array.regimenes_tributarios, android.R.layout.simple_spinner_item)
        adapterRegimen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRegimen.adapter = adapterRegimen

        val adapterRetencion = ArrayAdapter.createFromResource(this, R.array.opciones_si_no, android.R.layout.simple_spinner_item)
        adapterRetencion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRetieneHonorarios.adapter = adapterRetencion
    }

    private fun rellenarFormulario(e: EmpresaDTO) {
        binding.etRazonSocial.setText(e.razonSocial)
        binding.etRut.setText(e.rutEmpresa)
        binding.etGiro.setText(e.giro)
        binding.etTipoEmpresa.setText(e.tipoEmpresa)
        binding.etFechaInicio.setText(e.fechaInicioActividades)
        binding.etDireccion.setText(e.direccionComercial)
        binding.etCorreo.setText(e.correoContacto)
        binding.etTelefono.setText(e.telefonoContacto)

        when (e.estadoEmpresa?.lowercase()) {
            "activa" -> binding.spEstado.setSelection(0)
            "suspendida" -> binding.spEstado.setSelection(1)
            "cerrada" -> binding.spEstado.setSelection(2)
        }
        
        binding.spAfectaIva.setSelection(if (e.activa == true) 1 else 2)
        binding.spRegimen.setSelection(0)
        binding.spRetieneHonorarios.setSelection(0)
    }

    private fun clean(value: String?): String? = value?.trim()?.takeIf { it.isNotEmpty() }

    private fun validarYGuardar() {
        if (isSubmitting) return

        val razon = clean(binding.etRazonSocial.text?.toString()) ?: ""
        val rut = clean(binding.etRut.text?.toString()) ?: ""
        val userId = sessionManager.getUserId()

        if (razon.isBlank()) {
            binding.etRazonSocial.error = "Razón social obligatoria"
            return
        }
        if (rut.isBlank()) {
            binding.etRut.error = "RUT obligatorio"
            return
        }

        isSubmitting = true
        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "Procesando..."

        val estadoActual = when(binding.spEstado.selectedItemPosition) {
            0 -> "activa"
            1 -> "suspendida"
            else -> "cerrada"
        }
        val afectaIvaBool = binding.spAfectaIva.selectedItemPosition == 1
        val regimenSeleccionado = when (binding.spRegimen.selectedItemPosition) {
            1 -> "14D3"
            2 -> "14D8"
            3 -> "14A"
            else -> null
        }

        val api = RetrofitClient.getApi(this)
        
        if (empresaExistente == null) {
            val request = CreateEmpresaRequest(
                nombre = razon,
                razonSocial = razon,
                rutEmpresa = rut,
                ownerId = userId,
                giro = clean(binding.etGiro.text?.toString()),
                tipoEmpresa = clean(binding.etTipoEmpresa.text?.toString()),
                fechaInicioActividades = clean(binding.etFechaInicio.text?.toString()),
                direccionComercial = clean(binding.etDireccion.text?.toString()),
                correoContacto = clean(binding.etCorreo.text?.toString()),
                telefonoContacto = clean(binding.etTelefono.text?.toString()),
                representanteLegal = clean(binding.etRepresentante.text?.toString()),
                regimenTributario = regimenSeleccionado,
                afectaIva = afectaIvaBool
            )
            api.crearEmpresaRpc(request).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    isSubmitting = false
                    binding.btnGuardar.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@EmpresaFormActivity, "Empresa creada", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    isSubmitting = false
                    binding.btnGuardar.isEnabled = true
                }
            })
        } else {
            val filters = mapOf("id" to "eq.${empresaExistente!!.id}")
            val updateFields = mutableMapOf<String, Any?>(
                "razon_social" to razon,
                "rut_empresa" to rut,
                "estado_empresa" to estadoActual,
                "activa" to afectaIvaBool,
                "regimen_tributario" to regimenSeleccionado,
                "giro" to clean(binding.etGiro.text?.toString()),
                "tipo_empresa" to clean(binding.etTipoEmpresa.text?.toString()),
                "direccion_comercial" to clean(binding.etDireccion.text?.toString())
            )
            api.editarEmpresa(filters, updateFields).enqueue(object : Callback<List<EmpresaDTO>> {
                override fun onResponse(call: Call<List<EmpresaDTO>>, response: Response<List<EmpresaDTO>>) {
                    isSubmitting = false
                    binding.btnGuardar.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@EmpresaFormActivity, "Empresa actualizada", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
                override fun onFailure(call: Call<List<EmpresaDTO>>, t: Throwable) {
                    isSubmitting = false
                    binding.btnGuardar.isEnabled = true
                }
            })
        }
    }
}
