package org.jbake.launcher

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.jbake.TestUtils
import org.jbake.app.configuration.JBakeConfiguration
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import java.util.concurrent.Executors

class JettyServerTest : StringSpec({

    lateinit var jBakeConfiguration: JBakeConfiguration

    beforeTest {
        jBakeConfiguration = mockk(relaxed = true)
    }

    "shouldRunWithCustomPortAndContext" {
        val tempDir = java.io.File.createTempFile("jbake", "test").apply {
            delete()
            mkdirs()
        }
        val out = java.io.File(tempDir, "build/jbake")
        out.mkdirs()

        val source = TestUtils.testResourcesAsSourceDir
        val port = JettyServerTest.getRandoPort()
        every { jBakeConfiguration.serverPort } returns port
        every { jBakeConfiguration.serverHostname } returns "localhost"
        every { jBakeConfiguration.serverContextPath } returns "/foo"

        val executorService = Executors.newSingleThreadExecutor()

        JettyServer().use { server ->
            executorService.execute { server.run(source.absolutePath, jBakeConfiguration) }
            while (!server.isStarted) {
                println("waiting until jetty is running")
                Thread.sleep(100)
            }

            val conn = URL("http://localhost:$port/foo/content/about.html").openConnection() as HttpURLConnection
            conn.setRequestMethod("GET")

            BufferedReader(InputStreamReader(conn.getInputStream())).readLine() shouldBe "title=About"
        }

        tempDir.deleteRecursively()
    }
}) {
    companion object {
        fun getRandoPort(): Int {
            ServerSocket(0).use { socket ->
                return socket.localPort
            }
        }
    }
}
