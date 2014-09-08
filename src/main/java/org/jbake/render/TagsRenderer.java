package org.jbake.render;

import java.io.File;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.Crawler;
import org.jbake.app.Renderer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class TagsRenderer implements RenderingTool {

	@Override
	public int render(Renderer renderer, ODatabaseDocumentTx db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
		if (config.getBoolean("render.tags")) {
			try {
				return renderer.renderTags(Crawler.getTags(db), config.getString("tag.path"));
			} catch (Exception e) {
				throw new RenderingException(e);
			}
		} else {
			return 0;
		}
	}

}
