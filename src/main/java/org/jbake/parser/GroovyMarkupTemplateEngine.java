package org.jbake.parser;

import groovy.lang.Writable;
import groovy.text.Template;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Renders the Groovy MarkupTemplateEngine file *.gmt.
 *  
 * @author Vadim Bauer
 */
public class GroovyMarkupTemplateEngine extends MarkupEngine { 
	private final static Logger LOG = LoggerFactory.getLogger(GroovyMarkupTemplateEngine.class);		
	
	MarkupTemplateEngine engine;
	
    public GroovyMarkupTemplateEngine() {
    	TemplateConfiguration config = new TemplateConfiguration();
    	engine = new MarkupTemplateEngine(config);
    }

    @Override
    public void processBody(final ParserContext context) {
    	LOG.info("Running Groovy Markup Template Engine for {}",context.getFile());
    	Template createdTemplate;
		try {
			String body = context.getBody();
			LOG.info("Reading {} characters",body.length());
			createdTemplate = engine.createTemplate(body);
			Writable result = createdTemplate.make();
			StringWriter newBodyStringWriter = new StringWriter();
			result.writeTo(newBodyStringWriter);
			String renderedBody = newBodyStringWriter.toString();
			LOG.info("Rendered {} characters",renderedBody.length());
			context.setBody(renderedBody);			
		} catch (CompilationFailedException e) {
			LOG.error("CompilationFailedException",e);
		} catch (ClassNotFoundException e) {
			LOG.error("ClassNotFoundException",e);
		} catch (IOException e) {
			LOG.error("IOException",e);
		}
    }
}
