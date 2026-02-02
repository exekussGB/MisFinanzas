package com.analistainacap.misfinanzas

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.analistainacap.misfinanzas.databinding.ActivityResumenContableBinding
import com.analistainacap.misfinanzas.network.*
import java.text.NumberFormat
import java.util.*

class ResumenContableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumenContableBinding
    private lateinit var viewModel: ResumenContableViewModel
    private val KEY_EMPRESA_ID = "empresa_id_activa"
    private var empresaIdActiva = ""
    private var anioActual = 0
    private var mesActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumenContableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        empresaIdActiva = prefs.getString(KEY_EMPRESA_ID, "") ?: ""
        if (empresaIdActiva.isEmpty()) {
            Toast.makeText(this, "Seleccione una empresa primero", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViewModel()
        setupUI()
        observeViewModel()

        val cal = Calendar.getInstance()
        anioActual = cal.get(Calendar.YEAR)
        mesActual = cal.get(Calendar.MONTH) + 1
        
        cargarDatos()
    }

    private fun setupViewModel() {
        val repository = ResumenContableRepository(RetrofitClient.getApi(this))
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ResumenContableViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[ResumenContableViewModel::class.java]
    }

    private fun setupUI() {
        binding.tvPeriodoActual.setOnClickListener { showMonthYearPicker() }
        binding.btnVolverResumen.setOnClickListener { finish() }
    }

    private fun cargarDatos() {
        val periodo = String.format(Locale("es", "CL"), "%d-%02d", anioActual, mesActual)
        binding.tvPeriodoActual.text = periodo
        viewModel.cargarDatos(empresaIdActiva, periodo)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.pbResumenContable.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.kpisGenerales.observe(this) { kpi ->
            Log.d("DATA_OK", "KPIs recibidos: $kpi")
            if (kpi != null) {
                setFormattedCurrency(binding.tvKpiResultado, kpi.resultadoPeriodo, true)
                setFormattedCurrency(binding.tvKpiIngresos, kpi.totalIngresos)
                setFormattedCurrency(binding.tvKpiEgresos, kpi.totalEgresos)
            } else if (viewModel.isLoading.value == false) {
                mostrarCeros()
            }
        }

        viewModel.ivaResumen.observe(this) { iva ->
            Log.d("DATA_OK", "IVA recibido: $iva")
            if (iva != null) {
                setFormattedCurrency(binding.tvKpiVentasNetas, iva.ventasNetas)
                setFormattedCurrency(binding.tvKpiIvaDebito, iva.ivaDebito)
                setFormattedCurrency(binding.tvKpiIvaCredito, iva.ivaCredito)

                val saldoPagar = iva.ivaPorPagar ?: 0.0
                if (saldoPagar > 0) {
                    binding.rowIvaPagar.visibility = View.VISIBLE
                    binding.rowRemanenteIva.visibility = View.GONE
                    setFormattedCurrency(binding.tvKpiIvaPagar, saldoPagar, true)
                } else {
                    binding.rowIvaPagar.visibility = View.GONE
                    binding.rowRemanenteIva.visibility = View.VISIBLE
                    setFormattedCurrency(binding.tvKpiRemanenteIva, iva.remanenteIva, true)
                }
            }
        }

        viewModel.error.observe(this) { err ->
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarCeros() {
        val zero = 0.0
        setFormattedCurrency(binding.tvKpiResultado, zero, true)
        setFormattedCurrency(binding.tvKpiIngresos, zero)
        setFormattedCurrency(binding.tvKpiEgresos, zero)
        setFormattedCurrency(binding.tvKpiVentasNetas, zero)
        setFormattedCurrency(binding.tvKpiIvaDebito, zero)
        setFormattedCurrency(binding.tvKpiIvaCredito, zero)
        binding.rowIvaPagar.visibility = View.GONE
        binding.rowRemanenteIva.visibility = View.GONE
    }

    private fun showMonthYearPicker() {
        DatePickerDialog(this, { _, year, month, _ ->
            anioActual = year
            mesActual = month + 1
            cargarDatos()
        }, anioActual, mesActual - 1, 1).show()
    }

    private fun setFormattedCurrency(textView: TextView, value: Double?, autoColor: Boolean = false) {
        val amount = value ?: 0.0
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        textView.text = format.format(amount)
        if (autoColor) {
            val color = if (amount < 0) R.color.error else R.color.success
            textView.setTextColor(getColor(color))
        }
    }
}
