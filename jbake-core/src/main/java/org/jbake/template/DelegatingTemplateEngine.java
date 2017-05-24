package org.jbake.template;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ContentStore;
import org.jbake.app.FileUtil;
import org.jbake.model.DocumentTypeUtils;
import org.jbake.model.DocumentTypes;
import org.jbake.template.model.PublishedCustomExtractor;
import org.jbake.template.model.TypedDocumentsExtractor;
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
    private final static Logger LOGGER = LoggerFactory.getLogger(DelegatingTemplateEngine.class);

    private final TemplateEngines renderers;

    public DelegatingTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        this.renderers = new TemplateEngines(config, db, destination, templatesPath);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, String templateName, final Writer writer) throws RenderingException {
        model.put("version", config.getString(Keys.VERSION));
        Map<String, Object> configModel = new HashMap<String, Object>();
        Iterator<String> configKeys = config.getKeys();
        while (configKeys.hasNext()) {
            String key = configKeys.next();
            //replace "." in key so you can use dot notation in templates
            configModel.put(key.replace(".", "_"), config.getProperty(key));
        }
        model.put("config", configModel);
        // if default template exists we will use it
        File templateFile = new File(templatesPath, templateName);
        if (!templateFile.exists()) {
            LOGGER.info("Default template: {} was not found, searching for others...", templateName);
            // if default template does not exist then check if any alternative engine templates exist
            String templateNameWithoutExt = templateName.substring(0, templateName.length() - 4);
            for (String extension : renderers.getRecognizedExtensions()) {
                templateFile = new File(templatesPath, templateNameWithoutExt + "." + extension);
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
