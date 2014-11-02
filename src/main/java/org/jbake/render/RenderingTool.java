package org.jbake.render;

import java.io.File;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.Renderer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public interface RenderingTool {

	int render(Renderer renderer, ODatabaseDocumentTx db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException;

}
