package org.jbake.spi;

import org.apache.commons.configuration.Configuration;
import org.jbake.launcher.LaunchOptions;

public interface Publisher {

    String publisherName();
    void publish(Configuration configuration, LaunchOptions options);
    
}
