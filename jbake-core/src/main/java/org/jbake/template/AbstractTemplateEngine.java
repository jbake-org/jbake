package org.jbake.template;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.jbake.engine.ModelExtractors;
import org.jbake.model.TemplateModel;

import java.io.File;
import java.io.Writer;


/**
 * A template is responsible for converting a model into a rendered document. The model
 * consists of key/value pairs, some of them potentially converted from a markup language
 * to HTML already.
 * <p>
 * An appropriate rendering engine will be chosen by JBake based on the template suffix. If
 * contents is not available in the supplied model, a template has access to the document
 * database in order to complete the model. It is in particular interesting to optimize
 * data access based on the underlying template engine capabilities.
 * <p>
 * Note that some rendering engines may rely on a different rendering model than the one
 * provided by the first argument of {@link #renderDocument(TemplateModel, String, Writer)}.
 * In this case, it is the responsibility of the engine to convert it.
 *
 * @author Cédric Champeau
 */
public abstract class AbstractTemplateEngine implements TemplateEngine {

    protected static ModelExtractors extractors = ModelExtractors.getInstance();
    protected final JBakeConfiguration config;
    protected final ContentStore db;

    /**
     * @deprecated use {@link AbstractTemplateEngine(JBakeConfiguration,ContentStore)} instead
     */
    @Deprecated
    protected AbstractTemplateEngine(final Configuration config, final ContentStore db, final File destination, final File templatesPath) {
        this(new JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(), destination, (CompositeConfiguration) config), db);
    }

    protected AbstractTemplateEngine(final JBakeConfiguration config, final ContentStore db) {
        this.config = config;
        this.db = db;
    }

}
