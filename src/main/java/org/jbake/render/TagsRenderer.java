package org.jbake.render;

import java.io.File;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.Renderer;
import org.jbake.template.RenderingException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class TagsRenderer implements RenderingTool {

	@Override
	public int render(Renderer renderer, ContentStore db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
		if (config.getBoolean(Keys.RENDER_TAGS)) {
			try {
				return renderer.renderTags(db.getTags(), config.getString(Keys.TAG_PATH));
			} catch (Exception e) {
				throw new RenderingException(e);
			}
		} else {
			return 0;
		}
	}

}