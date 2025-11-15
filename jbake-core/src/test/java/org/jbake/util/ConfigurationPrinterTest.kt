package org.jbake.util

import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class ConfigurationPrinterTest {
    @Test
    @Throws(Exception::class)
    fun shouldPrintHeader() {
        val configuration =
            JBakeConfigurationFactory().configUtil.loadConfig(TestUtils.getTestResourcesAsSourceFolder())
        val data = ByteArrayOutputStream()
        val out = PrintStream(data)
        val printer = ConfigurationPrinter(configuration, out)

        printer.print()

        Assertions.assertThat(data.toString()).contains("DEFAULT - Settings")
        Assertions.assertThat(data.toString()).contains("CUSTOM - Settings")
        Assertions.assertThat(data.toString()).contains("Key")
        Assertions.assertThat(data.toString()).contains("Value")
    }


    @Test
    @Throws(Exception::class)
    fun shouldPrintKeyAndValue() {
        val configuration =
            JBakeConfigurationFactory().configUtil.loadConfig(TestUtils.getTestResourcesAsSourceFolder())
        val data = ByteArrayOutputStream()
        val out = PrintStream(data)
        val printer = ConfigurationPrinter(configuration, out)

        printer.print()

        Assertions.assertThat(data.toString()).contains("site.host")
        Assertions.assertThat(data.toString()).contains("http://www.jbake.org")
    }
}
