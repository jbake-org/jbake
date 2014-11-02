package org.jbake.render;

import java.io.File;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.Renderer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class ArchiveRenderer implements RenderingTool {

	@Override
	public int render(Renderer renderer, ODatabaseDocumentTx db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
		if (config.getBoolean("render.archive")) {
			try {
				renderer.renderArchive(config.getString("archive.file"));
				return 1;
			} catch (Exception e) {
				throw new RenderingException(e);
			}
		} else {
			return 0;
		}
	}

}
