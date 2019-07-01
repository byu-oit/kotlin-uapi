package edu.byu.uapi.http.docs

import edu.byu.uapi.model.UAPIModel
import edu.byu.uapi.model.dialect.UAPIDialect
import edu.byu.uapi.model.serialization.UAPISerializationFormat
import java.io.File
import java.io.InputStream

interface DocSource {
    val name: String
    val contentType: String

    fun getInputStream(pretty: Boolean = false): InputStream
}

class DialectDocSource(
    private val uapiModel: UAPIModel,
    private val dialect: UAPIDialect<*>,
    private val format: UAPISerializationFormat
): DocSource {
    override val name: String = "${dialect.name}.${format.extension}"
    override val contentType: String = format.mime

    private val file: File by lazy { stashToFile(false) }
    private val prettyFile: File by lazy { stashToFile(true) }

    private fun stashToFile(pretty: Boolean): File {
        val p = if (pretty) "-pretty" else ""
        return File.createTempFile("uapi-model", "${dialect.name}$p.${format.extension}").apply {
            this.deleteOnExit()

            writer().use { dialect.convertAndWrite(uapiModel, it, format, pretty) }
        }
    }

    override fun getInputStream(pretty: Boolean): InputStream {
        return getFile(pretty).inputStream()
    }

    private fun getFile(pretty: Boolean): File = if (pretty) {
        prettyFile
    } else file
}
