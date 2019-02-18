package org.jbake.template;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.escaper.EscaperExtension;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders pages using the <a href="https://pebbletemplates.io/">Pebble</a> template engine.
 *
 * @author Mitchell Bosecke
 */
public class PebbleTemplateEngine extends AbstractTemplateEngine {
    private PebbleEngine engine;

    public PebbleTemplateEngine(final JBakeConfiguration config, final ContentStore db) {
        super(config, db);
        initializeTemplateEngine();
    }

    private void initializeTemplateEngine() {
        Loader loader = new FileLoader();
        loader.setPrefix(config.getTemplateFolder().getAbsolutePath());

        /*
         * Turn off the autoescaper because I believe that we can assume all
         * data is safe considering it is all statically generated.
         */
        EscaperExtension escaper = new EscaperExtension();
        escaper.setAutoEscaping(false);

        engine = new PebbleEngine.Builder().loader(loader).extension(escaper).build();
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
                String key = property.toString();
                try {
                    return extractors.extractAndTransform(db, key, this, new TemplateEngineAdapter.NoopAdapter());
                } catch(NoModelExtractorException e) {
                    // fallback to parent model
                }

                return super.get(property);
            }
        };

        return result;
    }
}
