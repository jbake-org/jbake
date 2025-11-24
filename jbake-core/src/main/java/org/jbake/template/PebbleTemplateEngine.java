package org.jbake.template;

import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.model.TemplateModel;

import java.io.IOException;
import java.io.Writer;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.escaper.EscaperExtension;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

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
        Loader<String> loader = new FileLoader();
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
    public void renderDocument(final TemplateModel model, final String templateName, final Writer writer)
        throws RenderingException {

        PebbleTemplate template;
        try {
            template = engine.getTemplate(templateName);
            template.evaluate(writer, wrap(model));
        } catch (PebbleException | IOException e) {
            throw new RenderingException(e);
        }

    }

    private TemplateModel wrap(final TemplateModel model) {
        return new TemplateModel(model) {

            private static final long serialVersionUID = -5489285491728950547L;

            @Override
            public Object get(final Object property) {
                try {
                    return extractors.extractAndTransform(db, (String) property, this, new TemplateEngineAdapter.NoopAdapter());
                } catch(NoModelExtractorException e) {
                    return super.get(property);
                }
            }
        };

    }
}
