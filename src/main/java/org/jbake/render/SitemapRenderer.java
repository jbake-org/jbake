package org.jbake.render;

import java.io.File;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.Renderer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class SitemapRenderer implements RenderingTool {

	@Override
	public int render(Renderer renderer, ODatabaseDocumentTx db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
		if (config.getBoolean("render.sitemap")) {
			try {
				renderer.renderSitemap(config.getString("sitemap.file"));
				return 1;
			} catch (Exception e) {
				throw new RenderingException(e);
			}
		} else {
			return 0;
		}
	}

}
