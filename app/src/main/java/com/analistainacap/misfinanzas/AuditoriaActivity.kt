package com.analistainacap.misfinanzas

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.analistainacap.misfinanzas.databinding.ActivityAuditoriaBinding
import com.analistainacap.misfinanzas.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Auditoría Contable (C7) - MODO LECTURA.
 * Sincronizado con contrato @QueryMap.
 */
class AuditoriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuditoriaBinding
    private val KEY_EMPRESA_ID = "empresa_id_activa"
    private val TAG = "AuditoriaContable"
    
    private var currentPage = 0
    private val pageSize = 50
    private var isLastPage = false
    private var isLoading = false
    private val auditLogs = mutableListOf<AuditoriaDTO>()
    private lateinit var adapter: AuditoriaAdapter
    private var empresaIdActiva = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuditoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        empresaIdActiva = prefs.getString(KEY_EMPRESA_ID, "") ?: ""
        val rol = prefs.getString("user_rol", "usuario") ?: "usuario"

        if (empresaIdActiva.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar una empresa activa", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!esRolAutorizado(rol)) {
            binding.tvEmptyAuditoria.text = "Permisos insuficientes"
            binding.tvEmptyAuditoria.visibility = View.VISIBLE
            binding.rvAuditoria.visibility = View.GONE
            binding.btnVolverAuditoria.setOnClickListener { finish() }
            return
        }

        setupRecyclerView()
        loadNextPage()

        binding.btnVolverAuditoria.setOnClickListener { finish() }
    }

    private fun esRolAutorizado(rol: String): Boolean {
        return rol == "admin" || rol == "owner" || rol == "contador" || rol == "administrador"
    }

    private fun setupRecyclerView() {
        adapter = AuditoriaAdapter(auditLogs)
        binding.rvAuditoria.layoutManager = LinearLayoutManager(this)
        binding.rvAuditoria.adapter = adapter

        binding.rvAuditoria.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!isLoading && !isLastPage) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == auditLogs.size - 1) {
                        loadNextPage()
                    }
                }
            }
        })
    }

    private fun loadNextPage() {
        isLoading = true
        binding.pbAuditoria.visibility = View.VISIBLE

        val rangeHeader = "${currentPage * pageSize}-${(currentPage + 1) * pageSize - 1}"
        val filters = mapOf("empresa_id" to "eq.$empresaIdActiva")

        RetrofitClient.getApi(this).getAuditoria(
            filters = filters,
            range = rangeHeader
        ).enqueue(object : Callback<List<AuditoriaDTO>> {
            override fun onResponse(call: Call<List<AuditoriaDTO>>, response: Response<List<AuditoriaDTO>>) {
                isLoading = false
                binding.pbAuditoria.visibility = View.GONE
                if (response.isSuccessful) {
                    val newLogs = response.body() ?: emptyList()
                    if (newLogs.isEmpty()) {
                        isLastPage = true
                        if (currentPage == 0) binding.tvEmptyAuditoria.visibility = View.VISIBLE
                    } else {
                        adapter.addLogs(newLogs)
                        currentPage++
                        if (newLogs.size < pageSize) isLastPage = true
                    }
                }
            }
            override fun onFailure(call: Call<List<AuditoriaDTO>>, t: Throwable) {
                isLoading = false
                binding.pbAuditoria.visibility = View.GONE
            }
        })
    }
}
