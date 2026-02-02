package com.analistainacap.misfinanzas

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.analistainacap.misfinanzas.databinding.ActivityMovimientoDetalleBinding
import com.analistainacap.misfinanzas.network.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Detalle de Movimiento (C3) - MODO LECTURA.
 * Implementación con arquitectura ViewModel + Repository.
 * UI tipo ficha con alineación Izquierda-Derecha.
 * Sincronizado con contrato de modelos (G4).
 */
class MovimientoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovimientoDetalleBinding
    private lateinit var viewModel: MovimientoViewModel
    private val KEY_EMPRESA_ID = "empresa_id_activa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovimientoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        observeViewModel()

        val movimientoId = intent.getStringExtra("EXTRA_MOVIMIENTO_ID") ?: ""
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val empresaId = prefs.getString(KEY_EMPRESA_ID, "") ?: ""

        if (movimientoId.isNotEmpty() && empresaId.isNotEmpty()) {
            viewModel.loadDetalle(empresaId, movimientoId)
        } else {
            binding.tvMensajeErrorDetalle.text = "Faltan identificadores de acceso."
            binding.layoutErrorDetalle.visibility = View.VISIBLE
            binding.pbCargandoDetalle.visibility = View.GONE
        }

        binding.btnVolverDetalle.setOnClickListener { finish() }
        binding.btnErrorVolver.setOnClickListener { finish() }
    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.getApi(this)
        val repository = MovimientoRepository(apiService)
        
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MovimientoViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[MovimientoViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.pbCargandoDetalle.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) {
                binding.scrollDetalle.visibility = View.GONE
                binding.layoutErrorDetalle.visibility = View.GONE
            }
        }

        viewModel.movimiento.observe(this) { mov ->
            if (mov != null) {
                renderFicha(mov)
                binding.scrollDetalle.visibility = View.VISIBLE
            } else if (viewModel.isLoading.value == false) {
                binding.tvMensajeErrorDetalle.text = "Movimiento no encontrado."
                binding.layoutErrorDetalle.visibility = View.VISIBLE
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                binding.tvMensajeErrorDetalle.text = errorMsg
                binding.layoutErrorDetalle.visibility = View.VISIBLE
            }
        }
    }

    private fun renderFicha(mov: MovimientoDTO) {
        val formatClp = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        
        binding.tvDetalleGlosa.text = mov.glosa
        binding.tvDetalleMonto.text = formatClp.format(mov.monto)
        
        // Color por tipo
        val color = if (mov.tipo.lowercase() == "ingreso") R.color.success else R.color.error
        binding.tvDetalleMonto.setTextColor(getColor(color))

        // IVA Logic sincronizada con DTO (C13.7)
        if (mov.afectaIva == true) {
            binding.rowNeto.visibility = View.VISIBLE
            binding.rowIva.visibility = View.VISIBLE
            binding.tvDetalleMontoNeto.text = mov.neto?.let { formatClp.format(it) } ?: "$0"
            binding.tvDetalleMontoIva.text = mov.iva?.let { formatClp.format(it) } ?: "$0"
        } else {
            binding.rowNeto.visibility = View.GONE
            binding.rowIva.visibility = View.GONE
        }

        binding.tvDetalleFecha.text = formatFecha(mov.fecha)
        binding.tvDetalleCategoria.text = mov.categoria ?: "N/A"
        binding.tvDetalleFormaPago.text = mov.formaPago ?: "N/A"
        binding.tvDetalleTipo.text = mov.tipo.replaceFirstChar { it.uppercase() }
    }

    private fun formatFecha(fechaRaw: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(fechaRaw)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            fechaRaw
        }
    }
}
