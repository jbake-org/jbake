package org.jbake.util;

import org.jbake.TestUtils;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationPrinterTest {

    @Test
    void shouldPrintHeader() throws Exception {
        JBakeConfiguration configuration = new JBakeConfigurationFactory().getConfigUtil().loadConfig(TestUtils.getTestResourcesAsSourceFolder());
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(data);
        ConfigurationPrinter printer = new ConfigurationPrinter(configuration, out);

        printer.print();

        assertThat(data.toString()).contains("DEFAULT - Settings");
        assertThat(data.toString()).contains("CUSTOM - Settings");
        assertThat(data.toString()).contains("Key");
        assertThat(data.toString()).contains("Value");
    }


    @Test
    void shouldPrintKeyAndValue() throws Exception {
        JBakeConfiguration configuration = new JBakeConfigurationFactory().getConfigUtil().loadConfig(TestUtils.getTestResourcesAsSourceFolder());
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(data);
        ConfigurationPrinter printer = new ConfigurationPrinter(configuration, out);

        printer.print();

        assertThat(data.toString()).contains("site.host");
        assertThat(data.toString()).contains("http://www.jbake.org");
    }
}
