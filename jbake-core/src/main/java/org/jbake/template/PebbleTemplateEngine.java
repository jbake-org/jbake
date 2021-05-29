package org.jbake.template;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.escaper.EscaperExtension;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.model.TemplateModel;

import java.io.IOException;
import java.io.Writer;

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
        FileLoader loader = new FileLoader();
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
            @Override
            public Object get(final Object property) {
                try {
                    return extractors.extractAndTransform(db, (String) property, this, TemplateEngineAdapter.NO_ADAPTER);
                } catch(NoModelExtractorException e) {
                    return super.get(property);
                }
            }
        };

    }
}
