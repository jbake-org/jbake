package org.jbake.parser;

import java.io.File;

import org.fusesource.scalate.RenderContext;
import org.fusesource.scalate.Template;
import org.fusesource.scalate.TemplateEngine;
import org.fusesource.scalate.TemplateSource;
import org.fusesource.scalate.support.CodeGenerator;
import org.fusesource.scalate.support.CustomExtensionTemplateSource;
import org.fusesource.scalate.support.StringTemplateSource;

import scala.collection.Traversable;
import scala.reflect.io.Directory;
import scala.reflect.io.PlainDirectory;


/**
 * 
 * Renders HAML or Jade style documents.
 *  
 * @author Vadim Bauer
 */
public class ScalateEngine extends MarkupEngine { 
	
	TemplateEngine engine;
	
    public ScalateEngine() {
		engine = new TemplateEngine(null ,null);
//		Traversable<File> sd = null;
//		engine = new TemplateEngine(sd,null);
//		engine.allowCaching();
//		engine.generatorForExtension("jade");
//        Class engineClass = PegDownProcessor.class;
//        assert engineClass!=null;
    }

    @Override
    public void processBody(final ParserContext context) {
//    	Traversable sourceDir = new PlainDirectory(new Directory(new File(context.getContentPath())));
    	StringTemplateSource source = new StringTemplateSource("jade", context.getBody());
    	CustomExtensionTemplateSource templateType = source.templateType("jade");
//		CodeGenerator codeGenerator = engine.generatorForExtension("jade");
		context.setBody(engine.layout(templateType));
    }
}
