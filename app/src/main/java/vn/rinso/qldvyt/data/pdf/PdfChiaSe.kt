package vn.rinso.qldvyt.data.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** Tác vụ Lưu / Chia sẻ / In cho file PDF local (dùng chung viewer + tủ PDF). */
object PdfChiaSe {

    fun uriChiaSe(ctx: Context, file: File): Uri =
        FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)

    fun chiaSe(ctx: Context, file: File) {
        val uri = uriChiaSe(ctx, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(intent, "Chia sẻ PDF qua (Zalo, Gmail…)"))
    }

    fun chepRaNgoai(ctx: Context, fileNguon: File, uriDich: Uri) {
        ctx.contentResolver.openOutputStream(uriDich, "w")?.use { out ->
            FileInputStream(fileNguon).use { inp -> inp.copyTo(out) }
            try { out.flush() } catch (_: Exception) { }
        } ?: error("Không mở được nơi lưu")
    }

    fun inPdf(ctx: Context, file: File, tenJob: String) {
        val quanLy = ctx.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttrs: PrintAttributes?, newAttrs: PrintAttributes?,
                cancellation: android.os.CancellationSignal?,
                callback: LayoutResultCallback?, extras: android.os.Bundle?,
            ) {
                if (cancellation?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                } else {
                    val info = PrintDocumentInfo.Builder(file.name)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()
                    callback?.onLayoutFinished(info, !(oldAttrs == newAttrs))
                }
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellation: android.os.CancellationSignal?,
                callback: WriteResultCallback?,
            ) {
                try {
                    FileInputStream(file).use { inp ->
                        FileOutputStream(destination?.fileDescriptor).use { out -> inp.copyTo(out) }
                    }
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        quanLy.print(tenJob, adapter, PrintAttributes.Builder().build())
    }
}
