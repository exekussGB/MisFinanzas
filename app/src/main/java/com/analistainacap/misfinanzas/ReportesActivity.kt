package com.analistainacap.misfinanzas

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityReportesBinding
import com.analistainacap.misfinanzas.network.SessionManager
import java.util.*

/**
 * Centro de Reportes Contables (C10).
 * UX Mejorada: Selector de período global y feedback de carga (C10.16).
 */
class ReportesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportesBinding
    private var periodoSeleccionado = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Validar Sesión y Rol (C10.13)
        val session = SessionManager(this)
        if (session.getRol() == SessionManager.ROLE_USUARIO) {
            Toast.makeText(this, "Permisos insuficientes", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Inicializar Período (C10.16)
        val cal = Calendar.getInstance()
        updatePeriodoUI(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

        binding.tvPeriodoReporte.setOnClickListener { showMonthYearPicker() }

        // 3. Listeners de Libros (C10.2)
        binding.btnLibroIngresos.setOnClickListener {
            abrirReporteMovimientos("eq.ingreso", "Libro de Ingresos")
        }

        binding.btnLibroEgresos.setOnClickListener {
            abrirReporteMovimientos("eq.egreso", "Libro de Egresos")
        }

        binding.btnLibroDiario.setOnClickListener {
            abrirReporteMovimientos(null, "Libro Diario")
        }

        // 4. Reportes Tributarios
        binding.btnResumenIva.setOnClickListener {
            val intent = Intent(this, ResumenContableActivity::class.java)
            intent.putExtra("EXTRA_PERIODO", periodoSeleccionado)
            startActivity(intent)
        }

        binding.btnReporteCategorias.setOnClickListener {
            abrirReporteMovimientos(null, "Reporte por Categoría")
        }

        binding.btnVolverDashboard.setOnClickListener { finish() }
    }

    private fun updatePeriodoUI(year: Int, month: Int) {
        periodoSeleccionado = String.format("%d-%02d", year, month)
        binding.tvPeriodoReporte.text = periodoSeleccionado
    }

    private fun showMonthYearPicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, _ ->
            updatePeriodoUI(year, month + 1)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1).show()
    }

    private fun abrirReporteMovimientos(tipo: String?, titulo: String) {
        binding.pbReportes.visibility = View.VISIBLE // Feedback de carga (C10.16)
        
        val intent = Intent(this, MovimientosListActivity::class.java)
        intent.putExtra("EXTRA_FILTRO_TIPO", tipo)
        intent.putExtra("EXTRA_TITULO_REPORTE", titulo)
        intent.putExtra("EXTRA_PERIODO", periodoSeleccionado)
        
        startActivity(intent)
        
        // Simulación de delay de salida para el feedback
        binding.root.postDelayed({ binding.pbReportes.visibility = View.GONE }, 500)
    }
}
