package org.jbake.util

import org.assertj.core.api.Assertions.assertThat
import org.jbake.TestUtils
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class ConfigurationPrinterTest {
    @Test
    fun shouldPrintHeader() {
        val configuration =
            JBakeConfigurationFactory().configUtil.loadConfig(TestUtils.testResourcesAsSourceFolder)
        val data = ByteArrayOutputStream()
        val out = PrintStream(data)
        val printer = ConfigurationPrinter(configuration, out)

        printer.print()

        assertThat(data.toString()).contains("DEFAULT - Settings")
        assertThat(data.toString()).contains("CUSTOM - Settings")
        assertThat(data.toString()).contains("Key")
        assertThat(data.toString()).contains("Value")
    }


    @Test
    fun shouldPrintKeyAndValue() {
        val configuration =
            JBakeConfigurationFactory().configUtil.loadConfig(TestUtils.testResourcesAsSourceFolder)
        val data = ByteArrayOutputStream()
        val out = PrintStream(data)
        val printer = ConfigurationPrinter(configuration, out)

        printer.print()

        assertThat(data.toString()).contains("site.host")
        assertThat(data.toString()).contains("http://www.jbake.org")
    }
}
