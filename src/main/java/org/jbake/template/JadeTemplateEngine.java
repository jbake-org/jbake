package org.jbake.template;


import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.filter.CDATAFilter;
import de.neuland.jade4j.filter.CssFilter;
import de.neuland.jade4j.filter.JsFilter;
import de.neuland.jade4j.model.JadeModel;
import de.neuland.jade4j.template.FileTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ContentStore;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private JadeConfiguration jadeConfiguration = new JadeConfiguration();

    public JadeTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);

        TemplateLoader loader = new FileTemplateLoader(templatesPath.getAbsolutePath() + File.separatorChar, config.getString(Keys.TEMPLATE_ENCODING));
        jadeConfiguration.setTemplateLoader(loader);
        jadeConfiguration.setMode(Jade4J.Mode.XHTML);
        jadeConfiguration.setPrettyPrint(true);
        jadeConfiguration.setFilter(FILTER_CDATA, new CDATAFilter());
        jadeConfiguration.setFilter(FILTER_SCRIPT, new JsFilter());
        jadeConfiguration.setFilter(FILTER_STYLE, new CssFilter());
        jadeConfiguration.getSharedVariables().put("formatter", new FormatHelper());
    }

    @Override
    public void renderDocument(Map<String, Object> model, String templateName, Writer writer) throws RenderingException {
        try {
            JadeTemplate template = jadeConfiguration.getTemplate(templateName);

            renderTemplate(template, model, writer);
        } catch (IOException e) {
            throw new RenderingException(e);
        }
    }

    public void renderTemplate(JadeTemplate template, Map<String, Object> model, Writer writer) throws JadeCompilerException {
        JadeModel jadeModel = wrap(jadeConfiguration.getSharedVariables());
        jadeModel.putAll(model);
        template.process(jadeModel, writer);
    }

    private JadeModel wrap(final Map<String, Object> model) {
        return new JadeModel(model) {

            @Override
            public Object get(final Object property) {
                String key = property.toString();
                try {
            		return extractors.extractAndTransform(db, key, model, new TemplateEngineAdapter.NoopAdapter());
            	} catch(NoModelExtractorException e) {
            		// fallback to parent model
            	}

                return super.get(property);
            }
        };
    }

    public static class FormatHelper {
        private Map<String, SimpleDateFormat> formatters = new HashMap<String, SimpleDateFormat>();

        public String format(Date date, String pattern) {
            if(date!=null && pattern!=null) {
                SimpleDateFormat df = formatters.get(pattern);

                if(df==null) {
                    df = new SimpleDateFormat(pattern);
                    formatters.put(pattern, df);
                }

                return df.format(date);
            } else {
                return "";
            }
        }

        public String escape(String s) {
            return StringEscapeUtils.escapeHtml(s);
        }
    }
}
