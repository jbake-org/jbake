package org.jbake.render;

import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.RenderingException;

public interface RenderingTool {

    int render(Renderer renderer, ContentStore db, JBakeConfiguration config) throws RenderingException;

}