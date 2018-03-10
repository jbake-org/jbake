package org.jbake.template;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.FileUtil;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    public void renderDocument(final Map<String, Object> model, String templateName, final Writer writer) throws RenderingException {
        model.put("version", config.getVersion());

        // TODO: create config model from configuration
        Map<String, Object> configModel = new HashMap<>();
        Iterator<String> configKeys = config.getKeys();
        while (configKeys.hasNext()) {
            String key = configKeys.next();
            Object valueObject;

            if ( key.equals(JBakeProperty.PAGINATE_INDEX) ){
                valueObject = config.getPaginateIndex();
            }
            else {
                valueObject = config.get(key);
            }
            //replace "." in key so you can use dot notation in templates
            configModel.put(key.replace(".", "_"), valueObject);
        }
        model.put("config", configModel);
        // if default template exists we will use it
        File templateFolder = config.getTemplateFolder();
        File templateFile = new File(templateFolder, templateName);
        if (!templateFile.exists()) {
            LOGGER.info("Default template: {} was not found, searching for others...", templateName);
            // if default template does not exist then check if any alternative engine templates exist
            String templateNameWithoutExt = templateName.substring(0, templateName.length() - 4);
            for (String extension : renderers.getRecognizedExtensions()) {
                templateFile = new File(templateFolder, templateNameWithoutExt + "." + extension);
                if (templateFile.exists()) {
                    LOGGER.info("Found alternative template file: {} using this instead", templateFile.getName());
                    templateName = templateFile.getName();
                    break;
                }
            }
        }
        String ext = FileUtil.fileExt(templateName);
        AbstractTemplateEngine engine = renderers.getEngine(ext);
        if (engine != null) {
            engine.renderDocument(model, templateName, writer);
        } else {
            LOGGER.error("Warning - No template engine found for template: {}", templateName);
        }
    }
}
