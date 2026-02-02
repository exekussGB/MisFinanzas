package com.analistainacap.misfinanzas.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel para el Panel Financiero (C4/C9).
 * Sincronizado con contrato estricto de DTOs (C2).
 */
class ResumenContableViewModel(private val repository: ResumenContableRepository) : ViewModel() {

    private val _kpisGenerales = MutableLiveData<KpiResumenMensualDTO?>()
    val kpisGenerales: LiveData<KpiResumenMensualDTO?> = _kpisGenerales

    private val _ivaResumen = MutableLiveData<KpiResumenMensualDTO?>()
    val ivaResumen: LiveData<KpiResumenMensualDTO?> = _ivaResumen

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarDatos(empresaId: String, periodo: String) {
        _isLoading.value = true
        _error.value = null

        // Carga de KPIs Generales
        repository.getKpisGenerales(empresaId, periodo, 
            onResult = {
                _kpisGenerales.value = it
                checkFinalizacion()
            },
            onError = {
                _error.value = it
                _isLoading.value = false
            }
        )

        // Carga de IVA (reutiliza KpiResumenMensualDTO consolidado)
        repository.getIvaResumen(empresaId, periodo,
            onResult = {
                _ivaResumen.value = it
                checkFinalizacion()
            },
            onError = {
                _error.value = it
                _isLoading.value = false
            }
        )
    }

    private fun checkFinalizacion() {
        if (_kpisGenerales.value != null || _ivaResumen.value != null) {
            _isLoading.value = false
        }
    }
}
