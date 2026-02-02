package com.analistainacap.misfinanzas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.analistainacap.misfinanzas.databinding.ActivityAuditoriaDetalleBinding
import com.analistainacap.misfinanzas.network.AuditoriaDTO
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

/**
 * Detalle de Auditoría (C7.5).
 * Muestra el contraste de datos en formato JSON legible.
 */
class AuditoriaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuditoriaDetalleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuditoriaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val log = intent.getSerializableExtra("EXTRA_AUDITORIA") as? AuditoriaDTO

        if (log == null) {
            finish()
            return
        }

        renderDetalle(log)

        binding.btnVolverLogs.setOnClickListener { finish() }
    }

    private fun renderDetalle(log: AuditoriaDTO) {
        val info = StringBuilder()
        info.append("Fecha: ${log.fechaEvento}\n")
        info.append("Usuario: ${log.usuarioEmail ?: "N/A"}\n")
        info.append("Tabla: ${log.tablaNombre}\n")
        info.append("Operación: ${log.operacion}\n")
        info.append("ID Registro: ${log.id}")

        binding.tvDetalleInfo.text = info.toString()

        // Formatear JSON para legibilidad (C7.5)
        binding.tvJsonAnterior.text = formatJson(log.datoAnterior)
        binding.tvJsonNuevo.text = formatJson(log.datoNuevo)
    }

    private fun formatJson(jsonRaw: String?): String {
        if (jsonRaw.isNullOrEmpty() || jsonRaw == "{}") return "Sin datos"
        return try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val je = JsonParser.parseString(jsonRaw)
            gson.toJson(je)
        } catch (e: Exception) {
            jsonRaw
        }
    }
}
