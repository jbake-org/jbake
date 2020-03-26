package org.jbake.app.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.net.URL;

public class Utf8PropertiesConfiguration extends PropertiesConfiguration {
    /**
     * The default encoding (UTF-8 as specified by http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html)
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    public Utf8PropertiesConfiguration(File file) throws ConfigurationException {
        super(file);
    }

    public Utf8PropertiesConfiguration(URL url) throws ConfigurationException {
        super(url);
    }

    @Override
    public String getEncoding() {
        return DEFAULT_ENCODING;
    }
}
