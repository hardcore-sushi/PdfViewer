package app.grapheneos.pdfviewer.loader

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.Formatter
import android.text.style.StyleSpan
import android.util.Log
import app.grapheneos.pdfviewer.R
import org.json.JSONException

class DocumentPropertiesLoader(
    private val context: Context,
    private val properties: String,
    private val numPages: Int,
    private val fileName: String,
    private val fileSize: Long,
) {

    fun loadAsList(): List<CharSequence> {
        return load().map { item ->
            val name = context.getString(item.key.nameResource)
            val value = item.value

            SpannableStringBuilder()
                .append(name)
                .append(":\n")
                .append(value)
                .apply {
                    setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        name.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
        }
    }

    private fun load(): Map<DocumentProperty, String> {
        val result = mutableMapOf<DocumentProperty, String>()
        result.addFileProperties()
        result.addPageSizeProperty()
        result.addPDFJsProperties()
        return result
    }

    private fun MutableMap<DocumentProperty, String>.addPageSizeProperty() {
        this[DocumentProperty.Pages] = java.lang.String.valueOf(numPages)
    }

    private fun MutableMap<DocumentProperty, String>.addFileProperties() {
        putAll(getFileProperties())
    }

    private fun MutableMap<DocumentProperty, String>.addPDFJsProperties() {
        putAll(getPDFJsProperties())
    }

    private fun getPDFJsProperties(): Map<DocumentProperty, String> {
        return try {
            PDFJsPropertiesToDocumentPropertyConverter(
                properties,
                context.getString(R.string.document_properties_invalid_date),
                parseExceptionListener = { parseException, value ->
                    Log.w(
                        DocumentPropertiesAsyncTaskLoader.TAG,
                        "${parseException.message} for $value at offset: ${parseException.errorOffset}"
                    )
                }
            ).convert()
        } catch (e: JSONException) {
            Log.w(
                DocumentPropertiesAsyncTaskLoader.TAG,
                "invalid properties"
            )
            emptyMap()
        }
    }

    private fun getFileProperties(): Map<DocumentProperty, String> {
        val collections = mutableMapOf<DocumentProperty, String>()
        collections[DocumentProperty.FileName] = fileName
        collections[DocumentProperty.FileSize] = Formatter.formatFileSize(context, fileSize)
        return collections
    }
}
