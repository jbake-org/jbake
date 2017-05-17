package org.jbake.app;

import java.util.Map;

import org.jbake.app.Crawler.Attributes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author Manik Magar
 *
 */
public class HtmlUtil {

	/**
	 * Image paths are specified as w.r.t. assets folder. This function prefix rootpath to all img src except 
	 * the ones that starts with http://, https:// or '/'.
	 * 
	 * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add rootpath.
	 * 
	 * @param fileContents
	 */
    public static void fixImageSourceUrls(Map<String, Object> fileContents){
    	
    	String htmlContent = fileContents.get(Attributes.BODY).toString();
    	
    	String rootPath = fileContents.get(Attributes.ROOTPATH).toString();
    	
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
			if((source.startsWith("http://") 
					|| source.startsWith("https://") || source.startsWith("/")) == false){
				String relativeSource = rootPath + source;
				img.attr("src", relativeSource);
			}
		}
    	
    	fileContents.put(Attributes.BODY, document.body().toString());
    }
	
}
