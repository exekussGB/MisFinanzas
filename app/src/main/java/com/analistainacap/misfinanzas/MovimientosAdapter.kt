package com.analistainacap.misfinanzas

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.analistainacap.misfinanzas.network.MovimientoDTO
import java.text.NumberFormat
import java.util.*

class MovimientosAdapter(private val movimientos: List<MovimientoDTO>) :
    RecyclerView.Adapter<MovimientosAdapter.MovimientoViewHolder>() {

    class MovimientoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGlosa: TextView = view.findViewById(R.id.tvGlosa)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvMonto: TextView = view.findViewById(R.id.tvMonto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movimiento, parent, false)
        return MovimientoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
        val mov = movimientos[position]
        holder.tvGlosa.text = mov.glosa
        holder.tvFecha.text = mov.fecha

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        holder.tvMonto.text = format.format(mov.monto ?: 0.0)

        // Sincronizado con DTO: tipoMovimiento (snake_case en Supabase)
        val tipo = mov.tipoMovimiento?.lowercase() ?: "egreso"
        if (tipo == "ingreso") {
            holder.tvMonto.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.success))
        } else {
            holder.tvMonto.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.error))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MovimientoDetalleActivity::class.java)
            intent.putExtra("EXTRA_MOVIMIENTO_ID", mov.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = movimientos.size
}
