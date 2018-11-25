package org.jbake.util;

import org.hamcrest.CoreMatchers;
import org.jbake.TestUtils;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertThat;

public class ConfigurationPrinterTest {

    @Test
    public void shouldPrintHeader() throws Exception {

        JBakeConfiguration configuration = new JBakeConfigurationFactory().getConfigUtil().loadConfig(TestUtils.getTestResourcesAsSourceFolder());
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(data);
        ConfigurationPrinter printer = new ConfigurationPrinter(configuration, out);

        printer.print();

        assertThat(data.toString(), CoreMatchers.containsString("DEFAULT - Settings"));
        assertThat(data.toString(), CoreMatchers.containsString("CUSTOM - Settings"));
        assertThat(data.toString(), CoreMatchers.containsString("Key"));
        assertThat(data.toString(), CoreMatchers.containsString("Value"));
    }


    @Test
    public void shouldPrintKeyAndValue() throws Exception {
        JBakeConfiguration configuration = new JBakeConfigurationFactory().getConfigUtil().loadConfig(TestUtils.getTestResourcesAsSourceFolder());
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(data);
        ConfigurationPrinter printer = new ConfigurationPrinter(configuration, out);

        printer.print();

        assertThat(data.toString(), CoreMatchers.containsString("site.host"));
        assertThat(data.toString(), CoreMatchers.containsString("http://www.jbake.org"));
    }
}