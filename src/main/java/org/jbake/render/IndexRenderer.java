package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.template.RenderingException;

import java.io.File;

public class IndexRenderer implements RenderingTool {

    @Override
    public int render(Renderer renderer, ContentStore db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
        if (config.getBoolean(Keys.RENDER_INDEX)) {
            try {
                if (shouldPaginateIndex(config)) {
                    renderer.renderIndexPaging(config.getString(Keys.INDEX_FILE));
                } else {
                    renderer.renderIndex(config.getString(Keys.INDEX_FILE));
                }
                return 1;
            } catch (Exception e) {
                throw new RenderingException(e);
            }
        } else {
            return 0;
        }
    }

    private boolean shouldPaginateIndex(CompositeConfiguration config) {
        return config.containsKey(Keys.PAGINATE_INDEX) && config.getBoolean(Keys.PAGINATE_INDEX);
    }

}