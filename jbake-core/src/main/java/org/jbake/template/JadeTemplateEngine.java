package org.jbake.template;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.filter.CDATAFilter;
import de.neuland.jade4j.filter.CssFilter;
import de.neuland.jade4j.filter.JsFilter;
import de.neuland.jade4j.model.JadeModel;
import de.neuland.jade4j.template.FileTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.text.StringEscapeUtils;
import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.model.TemplateModel;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Renders pages using the <a href="http://jade.org/">Jade</a> template language.
 *
 * @author Aleksandar Vidakovic
 * @author Mariusz Smykuła
 */
public class JadeTemplateEngine extends AbstractTemplateEngine {
    private static final String FILTER_CDATA = "cdata";
    private static final String FILTER_STYLE = "css";
    private static final String FILTER_SCRIPT = "js";

    private final JadeConfiguration jadeConfiguration = new JadeConfiguration();

    @Deprecated
    public JadeTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
    }

    public JadeTemplateEngine(final JBakeConfiguration config, final ContentStore db) {
        super(config, db);

        TemplateLoader loader = new FileTemplateLoader(config.getTemplateFolder().getPath() + File.separatorChar, config.getTemplateEncoding());
        jadeConfiguration.setTemplateLoader(loader);
        jadeConfiguration.setMode(Jade4J.Mode.XHTML);
        jadeConfiguration.setPrettyPrint(true);
        jadeConfiguration.setFilter(FILTER_CDATA, new CDATAFilter());
        jadeConfiguration.setFilter(FILTER_SCRIPT, new JsFilter());
        jadeConfiguration.setFilter(FILTER_STYLE, new CssFilter());
        jadeConfiguration.getSharedVariables().put("formatter", new FormatHelper());
    }

    @Override
    public void renderDocument(TemplateModel model, String templateName, Writer writer) throws RenderingException {
        try {
            JadeTemplate template = jadeConfiguration.getTemplate(templateName);

            renderTemplate(template, model, writer);
        } catch (IOException e) {
            throw new RenderingException(e);
        }
    }

    public void renderTemplate(JadeTemplate template, TemplateModel model, Writer writer) {
        JadeModel jadeModel = wrap(model);
        jadeModel.putAll(jadeConfiguration.getSharedVariables());
        template.process(jadeModel, writer);
    }

    private JadeModel wrap(final TemplateModel model) {
        return new JadeModel(model) {

            @Override
            public Object get(final Object property) {
                try {
                    return extractors.extractAndTransform(db, (String) property, this, new TemplateEngineAdapter.NoopAdapter());
                } catch (NoModelExtractorException e) {
                    return super.get(property);
                }
            }
        };
    }

    public static class FormatHelper {
        private final Map<String, SimpleDateFormat> formatters = new HashMap<>();

        public String format(Date date, String pattern) {
            if (date != null && pattern != null) {
                SimpleDateFormat df = formatters.get(pattern);

                if (df == null) {
                    df = new SimpleDateFormat(pattern);
                    formatters.put(pattern, df);
                }

                return df.format(date);
            } else {
                return "";
            }
        }

        public String escape(String s) {
            return StringEscapeUtils.escapeHtml4(s);
        }
    }
}
