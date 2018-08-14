package org.jbake.template;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;

import java.io.File;
import java.io.Writer;
import java.util.Map;


/**
 * A template is responsible for converting a model into a rendered document. The model
 * consists of key/value pairs, some of them potentially converted from a markup language
 * to HTML already.
 *
 * An appropriate rendering engine will be chosen by JBake based on the template suffix. If
 * contents is not available in the supplied model, a template has access to the document
 * database in order to complete the model. It is in particular interesting to optimize
 * data access based on the underlying template engine capabilities.
 *
 * Note that some rendering engines may rely on a different rendering model than the one
 * provided by the first argument of {@link #renderDocument(java.util.Map, String, java.io.Writer)}.
 * In this case, it is the responsibility of the engine to convert it.
 *
 * @author Cédric Champeau
 */
public abstract class AbstractTemplateEngine {

    protected static ModelExtractors extractors = ModelExtractors.getInstance();
    protected final JBakeConfiguration config;
    protected final ContentStore db;

    @Deprecated
    protected AbstractTemplateEngine(final Configuration config, final ContentStore db, final File destination, final File templatesPath) {
        this(new JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(),destination, (CompositeConfiguration) config),db);
    }

    protected AbstractTemplateEngine(final JBakeConfiguration config, final ContentStore db) {
        this.config = config;
        this.db = db;
    }

    public abstract void renderDocument(Map<String,Object> model, String templateName, Writer writer) throws RenderingException;
}
