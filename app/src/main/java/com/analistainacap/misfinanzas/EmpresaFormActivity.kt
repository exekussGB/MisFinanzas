package com.analistainacap.misfinanzas

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityEmpresaFormBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Actividad para crear o editar una empresa.
 * Implementa PASO FINAL: Eliminación de validación de userId en cliente.
 * El backend obtiene el usuario vía auth.uid() desde el Token JWT.
 */
class EmpresaFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresaFormBinding
    private var empresaExistente: EmpresaDTO? = null
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 DIAGNÓSTICO FINAL: Solo verificar existencia de JWT
        val currentToken = getSharedPreferences("auth", MODE_PRIVATE).getString("token", null)
        Log.e("DEBUG_SESSION", "TOKEN PRESENTE = ${currentToken != null}")

        setupSpinners()

        empresaExistente = intent.getSerializableExtra("EXTRA_EMPRESA") as? EmpresaDTO
        // PASO C4: Log modo inicial
        Log.e("DEBUG_MODE", "EMPRESA RECIBIDA = $empresaExistente")

        if (empresaExistente != null) {
            binding.tvFormTitle.text = "Editar Empresa"
            binding.btnGuardar.text = "Actualizar Cambios"
            rellenarFormulario(empresaExistente!!)
        } else {
            binding.tvFormTitle.text = "Nueva Empresa"
            binding.btnGuardar.text = "Crear Empresa"
        }

        binding.btnGuardar.setOnClickListener {
            // PASO C1: Click botón
            Log.e("DEBUG_FLOW", "CLICK GUARDAR ENTRÓ")
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
        
        // Paso A7: Sincronización con EmpresaDTO (Lectura)
        when (e.estadoEmpresa?.lowercase()) {
            "activa" -> binding.spEstado.setSelection(0)
            "suspendida" -> binding.spEstado.setSelection(1)
            "cerrada" -> binding.spEstado.setSelection(2)
        }
        
        // Uso de 'activa' para el spinner de IVA
        binding.spAfectaIva.setSelection(if (e.activa == true) 1 else 2)
        
        binding.spRegimen.setSelection(0)
        binding.spRetieneHonorarios.setSelection(0)
    }

    private fun clean(value: String?): String? = value?.trim()?.takeIf { it.isNotEmpty() }

    private fun validarYGuardar() {
        // PASO C1: Inicio método
        Log.e("DEBUG_FLOW", "validarYGuardar() EJECUTADO")
        
        if (isSubmitting) {
            Log.e("DEBUG_FLOW", "RETORNO POR isSubmitting true")
            return
        }

        // PASO C2: Antes de validar
        Log.e("DEBUG_FLOW", "ANTES DE VALIDAR CAMPOS")

        val razon = clean(binding.etRazonSocial.text?.toString()) ?: ""
        val rut = clean(binding.etRut.text?.toString()) ?: ""

        if (razon.isBlank()) {
            binding.etRazonSocial.error = "Razón social obligatoria"
            Log.e("DEBUG_FLOW", "RETORNO POR VALIDACIÓN: Razón vacía")
            return
        }
        if (rut.isBlank()) {
            binding.etRut.error = "RUT obligatorio"
            Log.e("DEBUG_FLOW", "RETORNO POR VALIDACIÓN: RUT vacío")
            return
        }

        // 🔹 PASO B3 (VALIDACIÓN RÁPIDA): Log obligatorio de Token
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("token", null)
        Log.e("DEBUG_TOKEN", "TOKEN = $token")

        if (token == null) {
            Toast.makeText(this, "Sesión no válida: Inicie sesión de nuevo", Toast.LENGTH_SHORT).show()
            Log.e("DEBUG_FLOW", "RETORNO POR VALIDACIÓN: Token null")
            return
        }

        isSubmitting = true
        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "Procesando..."

        val api = RetrofitClient.getApi(this)
        
        // PASO C4: Determinar modo
        Log.e("DEBUG_MODE", "MODO = ${if (empresaExistente == null) "CREAR" else "EDITAR"}")

        if (empresaExistente == null) {
            // 🔹 CREACIÓN VÍA RPC
            val request = CreateEmpresaRequest(
                razon_social = razon,
                rut_empresa = rut,
                giro = clean(binding.etGiro.text?.toString()),
                tipo_empresa = clean(binding.etTipoEmpresa.text?.toString()),
                direccion_comercial = clean(binding.etDireccion.text?.toString()),
                correo_contacto = clean(binding.etCorreo.text?.toString()),
                telefono_contacto = clean(binding.etTelefono.text?.toString())
            )
            
            // PASO C3: Llamada API
            Log.e("DEBUG_API", "LLAMANDO API CREAR EMPRESA (RPC)")
            api.crearEmpresaRpc(request).enqueue(object : Callback<UUIDResponse> {
                override fun onResponse(
                    call: Call<UUIDResponse>,
                    response: Response<UUIDResponse>
                ) {
                    isSubmitting = false
                    binding.btnGuardar.isEnabled = true
                    if (response.isSuccessful && response.body() != null) {
                        val nuevoId = response.body()!!.id
                        Log.d("DEBUG_API", "Empresa creada con ID: $nuevoId")
                        Toast.makeText(
                            this@EmpresaFormActivity,
                            "Empresa creada",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.e("DEBUG_API", "RPC Error: ${response.code()}")
                        Toast.makeText(
                            this@EmpresaFormActivity,
                            "Error al crear empresa",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                override fun onFailure(call: Call<UUIDResponse>, t: Throwable) {
                    isSubmitting = false
                    binding.btnGuardar.isEnabled = true
                    Log.e("DEBUG_API", "RPC crear_empresa onFailure", t)
                    Toast.makeText(
                        this@EmpresaFormActivity,
                        "Error de red",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            // 🔹 EDICIÓN VÍA PATCH (FIX EDITAR)
            val updateRequest = UpdateEmpresaRequest(
                razon_social = razon,
                rut_empresa = rut,
                giro = clean(binding.etGiro.text?.toString()),
                tipo_empresa = clean(binding.etTipoEmpresa.text?.toString()),
                direccion_comercial = clean(binding.etDireccion.text?.toString()),
                correo_contacto = clean(binding.etCorreo.text?.toString()),
                telefono_contacto = clean(binding.etTelefono.text?.toString())
            )
            
            // PASO C3: Llamada API
            Log.e("DEBUG_API", "LLAMANDO API EDITAR EMPRESA (PATCH)")
            api.editarEmpresa("eq.${empresaExistente!!.id}", updateRequest).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    isSubmitting = false
                    binding.btnGuardar.isEnabled = true
                    
                    // PASO C3: Log Respuesta
                    Log.e("DEBUG_API", "onResponse code=${response.code()}")
                    
                    if (!response.isSuccessful) {
                        Toast.makeText(this@EmpresaFormActivity, "Error ${response.code()} al actualizar empresa", Toast.LENGTH_LONG).show()
                        return
                    }

                    Toast.makeText(this@EmpresaFormActivity, "Empresa actualizada", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    isSubmitting = false
                    binding.btnGuardar.isEnabled = true
                    // PASO C3: Log Fallo
                    Log.e("DEBUG_API", "onFailure", t)
                    Toast.makeText(this@EmpresaFormActivity, "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
