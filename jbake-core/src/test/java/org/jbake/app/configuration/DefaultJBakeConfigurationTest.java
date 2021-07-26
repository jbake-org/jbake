package org.jbake.app.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DefaultJBakeConfigurationTest {

    @TempDir
    File root;

    private JBakeConfiguration jBakeConfig;

    @BeforeEach
    public void setup() {
        jBakeConfig = new ConfigUtil().loadConfig(root, null);
    }

    @Test
    public void testAddConfiguration() {
        Properties props = new Properties();
        props.setProperty("version", "overwritten"); // overwrite property from default.properties
        props.setProperty("foo", "bar");
        jBakeConfig.addConfiguration(props);
        assertEquals("overwritten", jBakeConfig.get("version"));
        assertEquals("bar", jBakeConfig.get("foo"));
    }

    @Test
    public void testSetProperty() {
        jBakeConfig.setProperty("version", "overwritten"); // overwrite property from default.properties
        jBakeConfig.setProperty("foo", "bar");
        assertEquals("overwritten", jBakeConfig.get("version"));
        assertEquals("bar", jBakeConfig.get("foo"));
    }
}
