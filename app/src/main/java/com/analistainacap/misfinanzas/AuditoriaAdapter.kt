package com.analistainacap.misfinanzas

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.analistainacap.misfinanzas.network.AuditoriaDTO

class AuditoriaAdapter(private val logs: MutableList<AuditoriaDTO>) :
    RecyclerView.Adapter<AuditoriaAdapter.AuditoriaViewHolder>() {

    class AuditoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivAuditoriaIcon)
        val tvOperacion: TextView = view.findViewById(R.id.tvAuditoriaOperacion)
        val tvFecha: TextView = view.findViewById(R.id.tvAuditoriaFecha)
        val tvTabla: TextView = view.findViewById(R.id.tvAuditoriaTabla)
        val tvUsuario: TextView = view.findViewById(R.id.tvAuditoriaUsuario)
        val tvAnterior: TextView = view.findViewById(R.id.tvDatoAnterior)
        val tvNuevo: TextView = view.findViewById(R.id.tvDatoNuevo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auditoria, parent, false)
        return AuditoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuditoriaViewHolder, position: Int) {
        val log = logs[position]
        holder.tvOperacion.text = log.operacion
        holder.tvFecha.text = log.fechaEvento
        holder.tvTabla.text = "Tabla: ${log.tablaNombre}"
        holder.tvUsuario.text = "Usuario: ${log.usuarioEmail ?: "N/A"}"
        holder.tvAnterior.text = log.datoAnterior?.take(50) ?: "-"
        holder.tvNuevo.text = log.datoNuevo?.take(50) ?: "-"

        // C7.8 - Íconos por acción y colores semánticos
        val context = holder.itemView.context
        val operacion = log.operacion ?: ""
        when (operacion.uppercase()) {
            "INSERT" -> {
                holder.ivIcon.setImageResource(android.R.drawable.ic_input_add)
                holder.tvOperacion.setTextColor(ContextCompat.getColor(context, R.color.success))
            }
            "UPDATE" -> {
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_edit)
                holder.tvOperacion.setTextColor(ContextCompat.getColor(context, R.color.primary))
            }
            "DELETE" -> {
                holder.ivIcon.setImageResource(android.R.drawable.ic_delete)
                holder.tvOperacion.setTextColor(ContextCompat.getColor(context, R.color.error))
            }
            else -> {
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                holder.tvOperacion.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, AuditoriaDetalleActivity::class.java)
            intent.putExtra("EXTRA_AUDITORIA", log)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = logs.size

    fun addLogs(newLogs: List<AuditoriaDTO>) {
        val startPos = logs.size
        logs.addAll(newLogs)
        notifyItemRangeInserted(startPos, newLogs.size)
    }

    fun clear() {
        logs.clear()
        notifyDataSetChanged()
    }
}
