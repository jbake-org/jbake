package org.jbake.template;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.escaper.EscaperExtension;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * Renders pages using the <a
 * href="http://www.mitchellbosecke.com/pebble">Pebble</a> template engine.
 *
 * @author Mitchell Bosecke
 */
public class PebbleTemplateEngine extends AbstractTemplateEngine {

    private PebbleEngine engine;
    private static ModelExtractors extractors = new ModelExtractors();

    public PebbleTemplateEngine(final CompositeConfiguration config, final ContentStore db,
            final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        initializeTemplateEngine(config, templatesPath);
    }

    private void initializeTemplateEngine(final CompositeConfiguration config, final File templatesPath) {
        Loader loader = new FileLoader();
        loader.setPrefix(templatesPath.getAbsolutePath());
        engine = new PebbleEngine(loader);

        /*
         * Turn off the autoescaper because I believe that we can assume all
         * data is safe considering it is all statically generated.
         */
        EscaperExtension escaper = engine.getExtension(EscaperExtension.class);
        escaper.setAutoEscaping(false);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer)
            throws RenderingException {

        PebbleTemplate template;
        try {
            template = engine.getTemplate(templateName);
            template.evaluate(writer, wrap(model));
        } catch (PebbleException e) {
            throw new RenderingException(e);
        } catch (IOException e) {
            throw new RenderingException(e);
        }

    }

    private Map<String, Object> wrap(final Map<String, Object> model) {
        Map<String, Object> result = new HashMap<String, Object>(model) {

            private static final long serialVersionUID = -5489285491728950547L;

            @Override
            public Object get(final Object property) {
                if (property instanceof String) {
                    String key = property.toString();
                    try {
                		return extractors.extractAndTransform(db, key, model, new TemplateEngineAdapter.NoopAdapter());
                	} catch(NoModelExtractorException e) {
                		// fallback to parent model
                	}
                }

                return super.get(property);
            }

            @Override
            public boolean containsKey(Object property) {
                if (property instanceof String) {
                    String key = property.toString();
                    List<String> lazyKeys = new ArrayList<String>();
                    lazyKeys.add(ContentStore.DATABASE);
                    lazyKeys.add(ContentStore.PUBLISHED_POSTS);
                    lazyKeys.add(ContentStore.PUBLISHED_PAGES);
                    lazyKeys.add(ContentStore.PUBLISHED_CONTENT);
                    lazyKeys.add(ContentStore.ALL_CONTENT);
                    lazyKeys.add(ContentStore.ALLTAGS);
                    lazyKeys.add(ContentStore.TAG_POSTS);
                    lazyKeys.add(ContentStore.PUBLISHED_DATE);

                    String[] documentTypes = DocumentTypes.getDocumentTypes();
                    for (String docType : documentTypes) {
                        lazyKeys.add(docType + "s");
                    }

                    if (lazyKeys.contains(key)) {
                        return true;
                    }
                }

                return super.containsKey(property);
            }
        };

        return result;
    }

}
