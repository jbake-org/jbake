package org.jbake.util;

import org.jbake.app.Crawler.Attributes;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;

/**
 * 
 * @author Manik Magar
 *
 */
public class HtmlUtil {

	/**
	 * Image paths are specified as w.r.t. assets folder. This function prefix site host to all img src except 
	 * the ones that starts with http://, https://.
	 * 
	 * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
	 * 
	 * @param fileContents	Map representing file contents
	 * @param configuration Configuration object
	 */

	//TODO: use JBakeConfiguration
    public static void fixImageSourceUrls(Map<String, Object> fileContents, JBakeConfiguration configuration){
    	
    	String htmlContent = fileContents.get(Attributes.BODY).toString();
        
    	String siteHost = (String) configuration.get("site.host");
    	
    	String uri = fileContents.get(Attributes.URI).toString();
    	
    	if(fileContents.get(Attributes.NO_EXTENSION_URI) != null){
    		uri = fileContents.get(Attributes.NO_EXTENSION_URI).toString();
    		
    		//remove trailing "/"
    		if(uri.endsWith("/")) {
    			uri = uri.substring(0, uri.length() - 1);
    		}
    		
    	}
    	
    	if(uri.contains("/")){
        	//strip that file name, leaving end "/"
        		uri = uri.substring(0, uri.lastIndexOf("/") + 1);
        }
    	
    	Document document = Jsoup.parseBodyFragment(htmlContent);
    	
    	Elements allImgs = document.getElementsByTag("img");
    	
    	for (Element img : allImgs) {
			String source = img.attr("src");
			
			if(source.startsWith("./")){
				// image relative to current content is specified,
				// lets add current url to it.
				source = source.replaceFirst("./", uri);
			}
			
			// Now add the root path
			if(!source.startsWith("http://") 
					&& !source.startsWith("https://")){
					
				if (!siteHost.endsWith("/") && !source.startsWith("/")) siteHost = siteHost.concat("/");
				
				String fullUrl = siteHost + source;
				
				img.attr("src", fullUrl);
				
			}
		}
    	
    	
    	//Use body().html() to prevent adding <body></body> from parsed fragment.
    	fileContents.put(Attributes.BODY, document.body().html());
    }
	
}
