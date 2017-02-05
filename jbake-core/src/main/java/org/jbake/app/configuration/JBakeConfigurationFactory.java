package org.jbake.app.configuration;

import org.apache.commons.configuration.ConfigurationException;

import java.io.File;

/**
 * A {@link JBakeConfiguration} factory
 */
public class JBakeConfigurationFactory {

    /**
     * Creates a {@link DefaultJBakeConfiguration}
     * @param sourceFolder The source folder of the project
     * @param destination The destination folder to render and copy files to
     * @param isClearCache Whether to clear database cache or not
     * @return A {@link DefaultJBakeConfiguration} by given parameters
     * @throws ConfigurationException if loading the configuration fails
     */
    public static JBakeConfiguration createDefaultJbakeConfiguration(File sourceFolder, File destination, boolean isClearCache) throws ConfigurationException {

        DefaultJBakeConfiguration configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        configuration.setDestinationFolder(destination);
        configuration.setClearCache(isClearCache);

        return configuration;
    }
}
