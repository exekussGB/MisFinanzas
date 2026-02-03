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

        binding.tvDetalleGlosa.text = mov.glosa ?: "Sin descripción"
        binding.tvDetalleMonto.text = formatClp.format(mov.monto ?: 0.0)

        val color = if (mov.tipoMovimiento?.lowercase() == "ingreso") {
            R.color.success
        } else {
            R.color.error
        }
        binding.tvDetalleMonto.setTextColor(getColor(color))

        if (mov.afectaIva == true) {
            binding.rowNeto.visibility = View.VISIBLE
            binding.rowIva.visibility = View.VISIBLE
            binding.tvDetalleMontoNeto.text =
                formatClp.format(mov.montoNeto ?: 0.0)
            binding.tvDetalleMontoIva.text =
                formatClp.format(mov.montoIva ?: 0.0)
        } else {
            binding.rowNeto.visibility = View.GONE
            binding.rowIva.visibility = View.GONE
        }

        // FIX nullable fecha
        binding.tvDetalleFecha.text =
            mov.fecha?.let { formatFecha(it) } ?: "Sin fecha"

        binding.tvDetalleCategoria.text =
            mov.categoriaNombre ?: "Sin categoría"

        binding.tvDetalleFormaPago.text =
            mov.formaPago ?: "No especificado"

        binding.tvDetalleTipo.text =
            mov.tipoMovimiento
                ?.replaceFirstChar { it.uppercase() }
                ?: "N/A"
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
