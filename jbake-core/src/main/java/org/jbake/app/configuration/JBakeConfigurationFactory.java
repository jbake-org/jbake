package org.jbake.app.configuration;

import org.apache.commons.configuration.ConfigurationException;

import java.io.File;

/**
 * A {@link JBakeConfiguration} factory
 */
public class JBakeConfigurationFactory {

    private ConfigUtil configUtil;

    public JBakeConfigurationFactory() {
        this.configUtil = new ConfigUtil();
    }

    /**
     * Creates a {@link DefaultJBakeConfiguration}
     * @param sourceFolder The source folder of the project
     * @param destination The destination folder to render and copy files to
     * @param isClearCache Whether to clear database cache or not
     * @return A {@link JBakeConfiguration} by given parameters
     * @throws ConfigurationException if loading the configuration fails
     */
    public JBakeConfiguration createDefaultJbakeConfiguration(File sourceFolder, File destination, boolean isClearCache) throws ConfigurationException {

        DefaultJBakeConfiguration configuration = (DefaultJBakeConfiguration) getConfigUtil().loadConfig(sourceFolder);
        configuration.setDestinationFolder(destination);
        configuration.setClearCache(isClearCache);

        return configuration;
    }

    public JBakeConfiguration createJettyJbakeConfiguration(File sourceFolder, File destinationFolder, boolean isClearCache) throws ConfigurationException {
        DefaultJBakeConfiguration configuration = (DefaultJBakeConfiguration) getConfigUtil().loadConfig(sourceFolder);
        configuration.setDestinationFolder(destinationFolder);
        configuration.setClearCache(isClearCache);
        configuration.setSiteHost("http://localhost:"+configuration.getServerPort());
        return configuration;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }

    public void setConfigUtil(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }
}
