package com.analistainacap.misfinanzas.network

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    private fun generateFileName(empresa: String, periodo: String, tipoReporte: String, extension: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val fechaHoy = sdf.format(Date())
        val empresaClean = empresa.lowercase().replace(" ", "")
        return "${empresaClean}_${periodo}_${tipoReporte}_$fechaHoy.$extension"
    }

    fun exportToPdf(context: Context, movimientos: List<MovimientoDTO>, nombreEmpresa: String, rutEmpresa: String, periodo: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        var y = 50f
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText(nombreEmpresa.uppercase(), 40f, y, paint)
        
        y += 20f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("RUT: $rutEmpresa", 40f, y, paint)
        canvas.drawText("Período: $periodo", 400f, y, paint)

        y += 40f
        paint.color = Color.LTGRAY
        canvas.drawRect(40f, y - 15f, 550f, y + 5f, paint)
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("FECHA", 45f, y, paint)
        canvas.drawText("GLOSA", 120f, y, paint)
        canvas.drawText("TIPO", 350f, y, paint)
        canvas.drawText("MONTO", 480f, y, paint)

        y += 25f
        paint.isFakeBoldText = false
        var totalIngresos = 0.0
        var totalEgresos = 0.0

        movimientos.forEach { mov ->
            if (y > 780) return@forEach
            canvas.drawText(mov.fecha ?: "-", 45f, y, paint)
            canvas.drawText((mov.glosa ?: "").take(30), 120f, y, paint)
            canvas.drawText((mov.tipoMovimiento ?: "").take(10), 350f, y, paint)
            canvas.drawText(format.format(mov.monto ?: 0.0), 480f, y, paint)
            
            if (mov.tipoMovimiento?.lowercase() == "ingreso") {
                totalIngresos += (mov.monto ?: 0.0)
            } else {
                totalEgresos += (mov.monto ?: 0.0)
            }
            y += 18f
        }

        y += 20f
        canvas.drawLine(40f, y, 550f, y, paint)
        y += 20f
        paint.isFakeBoldText = true
        canvas.drawText("RESUMEN:", 40f, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText("Total Ingresos: ${format.format(totalIngresos)}", 250f, y, paint)
        y += 15f
        canvas.drawText("Total Egresos: ${format.format(totalEgresos)}", 250f, y, paint)
        y += 15f
        paint.isFakeBoldText = true
        canvas.drawText("Resultado: ${format.format(totalIngresos - totalEgresos)}", 250f, y, paint)

        val sdfFooter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        paint.textSize = 8f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText("Generado por MisFinanzas App - ${sdfFooter.format(Date())}", 40f, 820f, paint)

        pdfDocument.finishPage(page)

        val fileName = generateFileName(nombreEmpresa, periodo, "historial", "pdf")
        val file = File(context.cacheDir, fileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            shareFile(context, file, "application/pdf")
        } catch (e: Exception) {
            Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    fun exportToExcel(context: Context, movimientos: List<MovimientoDTO>, periodo: String) {
        val csv = StringBuilder()
        csv.append("Fecha,Glosa,Tipo,Categoria,Monto CLP\n")
        movimientos.forEach { mov ->
            csv.append("${mov.fecha},\"${mov.glosa}\",${mov.tipoMovimiento},\"${mov.categoriaNombre}\",${(mov.monto ?: 0.0).toLong()}\n")
        }

        val fileName = generateFileName("reporte", periodo, "export", "csv")
        val file = File(context.cacheDir, fileName)
        try {
            file.writeText(csv.toString())
            shareFile(context, file, "text/csv")
        } catch (e: Exception) {
            Toast.makeText(context, "Error Excel", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
    }
}
