package org.jbake.render;

import java.io.File;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.RenderingException;

public interface RenderingTool {


    int render(Renderer renderer, ContentStore db, JBakeConfiguration config) throws RenderingException;

    @Deprecated
    //TODO: remove at 3.0.0
	int render(Renderer renderer, ContentStore db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException;

    /**
     * Does this renderer create a file that will be situated in the same directory as the source markup?
     * Serves to keep the URLs intact for renderers that do.
     */
	boolean isRendersInPlace();
}
