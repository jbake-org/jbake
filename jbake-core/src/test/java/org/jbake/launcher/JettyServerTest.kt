package org.jbake.launcher

import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.configuration.JBakeConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.Executors

@ExtendWith(MockitoExtension::class)
internal class JettyServerTest {
    @Mock
    var jBakeConfiguration: JBakeConfiguration? = null

    @Test
    fun shouldRunWithCustomPortAndContext(@TempDir output: Path) {
        val out = output.resolve("build/jbake").toFile()
        out.mkdirs()

        val source = TestUtils.testResourcesAsSourceFolder
        val port = this.randoport
        Mockito.`when`(jBakeConfiguration!!.serverPort).thenReturn(port)
        Mockito.`when`(jBakeConfiguration!!.serverHostname).thenReturn("localhost")
        Mockito.`when`(jBakeConfiguration!!.serverContextPath).thenReturn("/foo")

        val executorService = Executors.newSingleThreadExecutor()

        JettyServer().use { server ->
            executorService.execute { server.run(source.absolutePath, jBakeConfiguration!!) }
            while (!server.isStarted) {
                println("waiting until jetty is running")
                Thread.sleep(100)
            }

            val url = URL("http://localhost:$port/foo/content/about.html")
            val con = url.openConnection() as HttpURLConnection
            con.setRequestMethod("GET")

            val `in` = BufferedReader(InputStreamReader(con.getInputStream()))
            Assertions.assertThat(`in`.readLine()).isEqualTo("title=About")
        }
    }

    @get:Throws(Exception::class)
    private val randoport: Int
        get() {
            ServerSocket(0).use { socket ->
                return socket.getLocalPort()
            }
        }
}
