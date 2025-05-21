package org.jbake.parser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.PegdownExtensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;

/**
 * Renders documents in the Markdown format.
 *
 * @author Cédric Champeau
 */
public class MarkdownEngine extends MarkupEngine {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownEngine.class);
    private Set<Class<? extends Extension>> flexMarkExtentionClasses;
    
    public MarkdownEngine() {
    	loadFlexMakExtensionClasses();
    }
    
    private void loadFlexMakExtensionClasses() {
    	Reflections reflections = new Reflections("com.vladsch.flexmark.ext");
    	flexMarkExtentionClasses =
    			  reflections.getSubTypesOf(Extension.class);
    }

    @Override
    public void processBody(final ParserContext context) {
        List<String> mdExts = context.getConfig().getMarkdownExtensions();

        int extensions = PegdownExtensions.NONE;
        List<Extension> flexMakkExtensions = new ArrayList<Extension>();
        String cleanExtensionName = null;
        boolean addExt = true;
        
        for (String ext : mdExts) {
        	if (ext.startsWith("-")) {
        		cleanExtensionName = ext.substring(1);
        		addExt = false;
        	} else if (ext.startsWith("+")) {
        		cleanExtensionName = ext.substring(1);
            }else {
            	cleanExtensionName = ext;
            }
        	
        	int pegDownExtension = extensionFor(cleanExtensionName);
        	
        	if(pegDownExtension != PegdownExtensions.NONE) {
        		if (addExt) {
        			extensions = addExtension(extensions, extensionFor(ext));
                } else {
                	 extensions = removeExtension(extensions, pegDownExtension);
                }
        	}else {
        		Extension flexMarkExt = searchFlexMarkExtension(cleanExtensionName);
        		//store it, will be added *after* pegDown options added by adapter
        		if(null != flexMarkExt) {
        			flexMakkExtensions.add(flexMarkExt);
        		}
        	}
        }
        
        MutableDataSet allOptions = new MutableDataSet();
        
        DataHolder pegDownOptions = PegdownOptionsAdapter.flexmarkOptions(extensions);
        allOptions.setAll(pegDownOptions);
                
        Parser.addExtensions(allOptions, flexMakkExtensions.toArray(new Extension[0]));
        
        Parser parser = Parser.builder(allOptions).build();
        HtmlRenderer renderer = HtmlRenderer.builder(allOptions).build();

        Document document = parser.parse(context.getBody());
        context.setBody(renderer.render(document));
    }
    
    private Extension searchFlexMarkExtension(String name) {
    	Extension ext = null;
    	for (Class<?> anExtension : flexMarkExtentionClasses) {
    		if(anExtension.getSimpleName().equalsIgnoreCase(name+"Extension")) {
    			Method builder;
				try {
					builder = anExtension.getDeclaredMethod("create");
					Object loadedExtention = builder.invoke(null);
					ext = (Extension) loadedExtention;
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					logger.warn("Cannot instanciate extention '{}', this extention will be ignored", name);
				}
    			break;
    		}
    	}
    	return ext;
    }

    private int extensionFor(String name) {
        int extension = PegdownExtensions.NONE;

        try {
            Field extField = PegdownExtensions.class.getDeclaredField(name);
            extension = extField.getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.debug("Undeclared extension field '{}', for pegDown will try to search for standard FlexMark Extension", name);
        }
        return extension;
    }

    private int addExtension(int previousExtensions, int additionalExtension) {
        return previousExtensions | additionalExtension;
    }

    private int removeExtension(int previousExtensions, int unwantedExtension) {
        return previousExtensions & (~unwantedExtension);
    }

}
