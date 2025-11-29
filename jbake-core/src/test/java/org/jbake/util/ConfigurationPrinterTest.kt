package org.jbake.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import org.jbake.TestUtils
import org.jbake.app.configuration.JBakeConfigurationFactory
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class ConfigurationPrinterTest : StringSpec({

    "shouldPrintHeader" {
        val configuration =
            JBakeConfigurationFactory().configUtil.loadConfig(TestUtils.testResourcesAsSourceDir)
        val data = ByteArrayOutputStream()
        val out = PrintStream(data)
        val printer = ConfigurationPrinter(configuration, out)

        printer.print()

        val output = data.toString()
        output shouldContain "DEFAULT - Settings"
        output shouldContain "CUSTOM - Settings"
        output shouldContain "Key"
        output shouldContain "Value"
    }


    "shouldPrintKeyAndValue" {
        val configuration =
            JBakeConfigurationFactory().configUtil.loadConfig(TestUtils.testResourcesAsSourceDir)
        val data = ByteArrayOutputStream()
        val out = PrintStream(data)
        val printer = ConfigurationPrinter(configuration, out)

        printer.print()

        val output = data.toString()
        output shouldContain "site.host"
        output shouldContain "http://www.jbake.org"
    }
})
