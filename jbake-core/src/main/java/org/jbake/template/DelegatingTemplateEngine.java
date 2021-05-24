package org.jbake.template;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.FileUtil;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.model.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Writer;

import static org.jbake.app.configuration.PropertyList.PAGINATE_INDEX;

/**
 * A template which is responsible for delegating to a supported template engine,
 * based on the file extension.
 *
 * @author Cédric Champeau
 */
public class DelegatingTemplateEngine extends AbstractTemplateEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingTemplateEngine.class);

    private final TemplateEngines renderers;

    /**
     * @deprecated Use {@link #DelegatingTemplateEngine(ContentStore, JBakeConfiguration)} instead.
     *
     * @param config the {@link CompositeConfiguration} of jbake
     * @param db the {@link ContentStore}
     * @param destination the destination path
     * @param templatesPath the templates path
     */
    @Deprecated
    public DelegatingTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        this.renderers = new TemplateEngines(this.config, db);
    }

    public DelegatingTemplateEngine(final ContentStore db, final JBakeConfiguration config) {
        super(config, db);
        this.renderers = new TemplateEngines(config, db);
    }

    @Override
    public void renderDocument(final TemplateModel model, final String templateName, final Writer writer) throws RenderingException {
        model.setVersion(config.getVersion());
        model.setConfig(config.asHashMap());

        // if default template exists we will use it
        File templateFolder = config.getTemplateFolder();
        File templateFile = new File(templateFolder, templateName);
        String theTemplateName = templateName;
        if (!templateFile.exists()) {
            LOGGER.info("Default template: {} was not found, searching for others...", templateName);
            // if default template does not exist then check if any alternative engine templates exist
            String templateNameWithoutExt = templateName.substring(0, templateName.length() - 4);
            for (String extension : renderers.getRecognizedExtensions()) {
                templateFile = new File(templateFolder, templateNameWithoutExt + "." + extension);
                if (templateFile.exists()) {
                    LOGGER.info("Found alternative template file: {} using this instead", templateFile.getName());
                    theTemplateName = templateFile.getName();
                    break;
                }
            }
        }
        String ext = FileUtil.fileExt(theTemplateName);
        AbstractTemplateEngine engine = renderers.getEngine(ext);
        if (engine != null) {
            engine.renderDocument(model, theTemplateName, writer);
        } else {
            LOGGER.error("Warning - No template engine found for template: {}", theTemplateName);
        }
    }
}
