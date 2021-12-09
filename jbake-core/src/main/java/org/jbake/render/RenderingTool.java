package org.jbake.render;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.exception.RenderingException;

import java.io.File;

public interface RenderingTool {


    int render(Renderer renderer, ContentStore db, JBakeConfiguration config) throws RenderingException;

    @Deprecated
    //TODO: remove at 3.0.0
    int render(Renderer renderer, ContentStore db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException;

}
