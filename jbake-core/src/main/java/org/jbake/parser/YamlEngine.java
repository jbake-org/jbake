package org.jbake.parser;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YamlEngine extends MarkupEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlEngine.class);

    public static final String JBAKE_PREFIX = "jbake-";

    /**
     * Parses the YAML file and ensures the output is always a Map.
     *
     * @param file
     * @return
     */
    @SuppressWarnings("unchecked")
    private DocumentModel parseFile(File file) {
        DocumentModel model = new DocumentModel();
        Yaml yaml = new Yaml();
        try (InputStream is = new FileInputStream(file)) {
            Object result = yaml.load(is);
            if (result instanceof List) {
                model.put("data", result);
            } else if (result instanceof Map) {
                model.putAll((Map<? extends String, ?>) result);
            } else {
                LOGGER.warn("Unexpected result [{}] while parsing YAML file {}", result.getClass(), file);
            }
        } catch (IOException e) {
            LOGGER.error("Error while parsing YAML file {}", file, e);
        }
        return model;
    }

    @Override
    public DocumentModel parse(JBakeConfiguration config, File file) {
        return parseFile(file);
    }

    /**
     * This method implements the contract allowing use of Yaml files as content files
     *
     * @param context the parser context
     */
    @Override
    public void processHeader(final ParserContext context) {
        DocumentModel fileContents = parseFile(context.getFile());
        DocumentModel documentModel = context.getDocumentModel();

        fileContents.keySet().forEach(key -> {
            if (hasJBakePrefix(key)) {
                String pKey = key.substring(6);
                documentModel.put(pKey, fileContents.get(key));
            } else {
                documentModel.put(key, fileContents.get(key));
            }
        });
    }

    /**
     * This method implements the contract allowing use of Yaml files as content files. As such there is
     * no body for Yaml files so this method just sets an empty String as the body.
     *
     * @param context the parser context
     */
    @Override
    public void processBody(ParserContext context) {
        context.setBody("");
    }

    private boolean hasJBakePrefix(String key) {
        return key.startsWith(JBAKE_PREFIX);
    }
}
