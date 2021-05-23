package org.jbake.parser;

import org.apache.commons.io.IOUtils;
import org.jbake.app.FileUtil;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.swing.text.Document;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
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
    private DocumentModel parseFile(File file) {
        DocumentModel model = new DocumentModel();
        Yaml yaml = new Yaml();
        try (InputStream is = new FileInputStream(file)) {
            Object result = yaml.load(is);
            model.setType("data");
            if (result instanceof List) {
                model.put("data", result);
            } else if (result instanceof Map) {
                model.putAll((Map)result);
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

        for (String key : fileContents.keySet()) {
            if (hasJBakePrefix(key)) {
                String pKey = key.substring(6);
                documentModel.put(pKey, fileContents.get(key));
            } else {
                documentModel.put(key, fileContents.get(key));
            }
        }
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

    @Override
    public String buildURI(JBakeConfiguration config, File file) {
        String uri = FileUtil.asPath(file).replace(FileUtil.asPath(config.getDataFolder()), "");
        // strip off leading /
        if (uri.startsWith(FileUtil.URI_SEPARATOR_CHAR)) {
            uri = uri.substring(1, uri.length());
        }
        return uri;
    }
}
