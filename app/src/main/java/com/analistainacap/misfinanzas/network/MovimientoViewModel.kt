package com.analistainacap.misfinanzas.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MovimientoViewModel(private val repository: MovimientoRepository) : ViewModel() {

    private val _movimiento = MutableLiveData<MovimientoDTO?>()
    val movimiento: LiveData<MovimientoDTO?> = _movimiento

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDetalle(empresaId: String, movimientoId: String) {
        _isLoading.value = true
        repository.getDetalleMovimiento(
            empresaId,
            movimientoId,
            onResult = {
                _movimiento.value = it
                _isLoading.value = false
            },
            onError = {
                _error.value = it
                _isLoading.value = false
            }
        )
    }
}
