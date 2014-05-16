package org.jbake.plugins;

import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;

public interface ContentPlugin {
	
	public void parseMarkdown(Map<String, Object> content, CompositeConfiguration config) throws ContentPluginException;

}
