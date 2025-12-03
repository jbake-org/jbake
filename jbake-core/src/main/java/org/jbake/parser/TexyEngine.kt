package org.jbake.parser

import org.jbake.template.RenderingException
import org.jbake.util.Logging.logger
import org.slf4j.Logger
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

/**
 * Renders documents in the Texy format using a Texy service endpoint.
 *
 * Texy is a markup language from the Czech Republic that converts plain text to XHTML.
 * This engine communicates with a Texy service (typically running in Docker or as a standalone service)
 * via HTTP to process Texy markup.
 *
 * Configuration:
 * - `texy.service.url`: The URL of the Texy service endpoint (default: http://localhost:8080/texy)
 * - `texy.connection.timeout`: Connection timeout in milliseconds (default: 5000)
 * - `texy.read.timeout`: Read timeout in milliseconds (default: 10000)
 *
 * The service is expected to accept POST requests with the Texy markup in the request body
 * and return the rendered HTML.
 */
class TexyEngine : MarkupEngine() {

    override fun processBody(parserContext: ParserContext) {
        val config = parserContext.config
        val serviceUrl = config.get("texy.service.url") as? String ?: DEFAULT_SERVICE_URL
        val connectionTimeout = (config.get("texy.connection.timeout") as? Number)?.toInt() ?: DEFAULT_CONNECTION_TIMEOUT
        val readTimeout = (config.get("texy.read.timeout") as? Number)?.toInt() ?: DEFAULT_READ_TIMEOUT

        try {
            log.debug("Processing Texy content using service at: $serviceUrl")
            val renderedHtml = renderTexy(parserContext.body, serviceUrl, connectionTimeout, readTimeout)
            parserContext.body = renderedHtml
            log.debug("Successfully rendered Texy content")
        }
        catch (e: Exception) {
            log.error("Error rendering Texy content: ${e.message}", e)
            // Fallback: keep the original content wrapped in a pre tag to indicate processing failed
            parserContext.body = "<pre>Error rendering Texy content: ${e.message}\n\n${parserContext.body}</pre>"
        }
    }

    /**
     * Renders Texy markup by sending it to the Texy service endpoint.
     *
     * @param texyContent The Texy markup content to render
     * @param serviceUrl The URL of the Texy service
     * @param connectionTimeout Connection timeout in milliseconds
     * @param readTimeout Read timeout in milliseconds
     * @return The rendered HTML content
     */
    private fun renderTexy(
        texyContent: String,
        serviceUrl: String,
        connectionTimeout: Int,
        readTimeout: Int
    ): String {
        val url = URI(serviceUrl).toURL()
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = connectionTimeout
            connection.readTimeout = readTimeout
            connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8")
            connection.setRequestProperty("Accept", "text/html; charset=UTF-8")

            // Send the Texy content

            try {
                connection.outputStream.use { outputStream ->
                    outputStream.write(texyContent.toByteArray(StandardCharsets.UTF_8))
                    outputStream.flush()
                }
            }
            catch (e: ConnectException) {
                throw RenderingException("Could not connect to Texy service at $serviceUrl: ${e.message}")
            }

            // Check response code
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw RenderingException("Texy service returned HTTP $responseCode: ${connection.responseMessage}")
            }

            // Read the response
            connection.inputStream.use { inputStream ->
                inputStream.readBytes().toString(StandardCharsets.UTF_8)
            }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val DEFAULT_SERVICE_URL = "http://localhost:8080/texy"
        private const val DEFAULT_CONNECTION_TIMEOUT = 5000 // 5 seconds
        private const val DEFAULT_READ_TIMEOUT = 10000 // 10 seconds

        private val log: Logger by logger()
    }
}


