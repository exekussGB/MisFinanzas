package com.analistainacap.misfinanzas

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.analistainacap.misfinanzas.databinding.ActivityMovimientosListBinding
import com.analistainacap.misfinanzas.network.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Listado de Movimientos (C2) con Sistema de Filtros Avanzados (C5).
 * Implementa BottomSheet para filtros combinables y persistentes.
 */
class MovimientosListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovimientosListBinding
    private val KEY_EMPRESA_ID = "empresa_id_activa"
    private val TAG = "MovimientosList"
    
    // Paginación
    private var currentPage = 0
    private val pageSize = 20
    private var isLastPage = false
    private var isLoading = false
    private val movimientosList = mutableListOf<MovimientoDTO>()
    private lateinit var adapter: MovimientosAdapter

    // ESTADO DE FILTROS (C5.5 - Persistentes durante la sesión de la actividad)
    private var filterTipo: String? = null
    private var filterPeriodo: String? = null
    private var filterAfectaIva: String? = null
    private var empresaIdActiva = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovimientosListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        empresaIdActiva = prefs.getString(KEY_EMPRESA_ID, "") ?: ""

        if (empresaIdActiva.isEmpty()) {
            finish()
            return
        }

        setupRecyclerView()

        // Botón para abrir BottomSheet de Filtros (C5.6)
        binding.btnFiltroTodo.text = "Filtros Avanzados"
        binding.btnFiltroTodo.setOnClickListener { showFiltersBottomSheet() }

        binding.btnReintentar.setOnClickListener { resetAndReload() }

        loadNextPage()
    }

    private fun setupRecyclerView() {
        adapter = MovimientosAdapter(movimientosList)
        binding.rvMovimientosFull.layoutManager = LinearLayoutManager(this)
        binding.rvMovimientosFull.adapter = adapter

        binding.rvMovimientosFull.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!isLoading && !isLastPage) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == movimientosList.size - 1) {
                        loadNextPage()
                    }
                }
            }
        })
    }

    private fun showFiltersBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_filtros_movimientos, null)
        dialog.setContentView(view)

        val spinnerTipo = view.findViewById<Spinner>(R.id.spinnerTipo)
        val tvPeriodo = view.findViewById<TextView>(R.id.tvFiltroPeriodo)
        val cbIva = view.findViewById<CheckBox>(R.id.cbAfectaIva)
        val btnAplicar = view.findViewById<Button>(R.id.btnAplicarFiltros)
        val btnLimpiar = view.findViewById<Button>(R.id.btnLimpiarFiltros)

        // Configurar Spinner Tipo
        val tipos = arrayOf("Todos", "Ingresos", "Egresos")
        spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        
        // Restaurar estado persistente (C5.5)
        when (filterTipo) {
            "eq.ingreso" -> spinnerTipo.setSelection(1)
            "eq.egreso" -> spinnerTipo.setSelection(2)
            else -> spinnerTipo.setSelection(0)
        }
        tvPeriodo.text = filterPeriodo?.replace("eq.", "") ?: "Seleccionar..."
        cbIva.isChecked = filterAfectaIva == "eq.true"

        tvPeriodo.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, _ ->
                val p = String.format("%d-%02d", year, month + 1)
                filterPeriodo = "eq.$p"
                tvPeriodo.text = p
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1).show()
        }

        btnAplicar.setOnClickListener {
            // Capturar Tipo
            filterTipo = when (spinnerTipo.selectedItemPosition) {
                1 -> "eq.ingreso"
                2 -> "eq.egreso"
                else -> null
            }
            // Capturar IVA
            filterAfectaIva = if (cbIva.isChecked) "eq.true" else null

            resetAndReload()
            dialog.dismiss()
        }

        btnLimpiar.setOnClickListener {
            filterTipo = null
            filterPeriodo = null
            filterAfectaIva = null
            resetAndReload()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun resetAndReload() {
        currentPage = 0
        isLastPage = false
        movimientosList.clear()
        adapter.notifyDataSetChanged()
        binding.layoutEmpty.visibility = View.GONE
        loadNextPage()
    }

    private fun loadNextPage() {
        if (isLoading) return
        isLoading = true
        binding.pbLoading.visibility = View.VISIBLE

        val range = "${currentPage * pageSize}-${(currentPage + 1) * pageSize - 1}"

        // Construcción del mapa de filtros para cumplir con el contrato de la API (C5.8)
        val filters = mutableMapOf<String, String>()
        filters["empresa_id"] = "eq.$empresaIdActiva"
        filterTipo?.let { filters["tipo_movimiento"] = it }
        filterPeriodo?.let { filters["periodo"] = it }
        filterAfectaIva?.let { filters["afecta_iva"] = it }

        RetrofitClient.getApi(this).getVistaMovimientos(
            filters = filters,
            range = range
        ).enqueue(object : Callback<List<MovimientoDTO>> {
            override fun onResponse(call: Call<List<MovimientoDTO>>, response: Response<List<MovimientoDTO>>) {
                isLoading = false
                binding.pbLoading.visibility = View.GONE

                if (response.isSuccessful) {
                    val nuevosItems = response.body() ?: emptyList()
                    manejarResultado(nuevosItems)
                } else {
                    mostrarErrorControlado("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<MovimientoDTO>>, t: Throwable) {
                isLoading = false
                binding.pbLoading.visibility = View.GONE
                mostrarErrorControlado("Sin conexión a internet")
            }
        })
    }

    private fun manejarResultado(items: List<MovimientoDTO>) {
        if (items.isEmpty()) {
            if (currentPage == 0) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.tvEmptyMessage.text = "Sin resultados para estos filtros"
            }
            isLastPage = true
        } else {
            binding.layoutEmpty.visibility = View.GONE
            movimientosList.addAll(items)
            adapter.notifyDataSetChanged()
            currentPage++
            if (items.size < pageSize) isLastPage = true
        }
    }

    private fun mostrarErrorControlado(mensaje: String) {
        if (movimientosList.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.tvEmptyMessage.text = mensaje
        } else {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }
}
