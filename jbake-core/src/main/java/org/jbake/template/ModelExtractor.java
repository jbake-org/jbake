package org.jbake.template;

import java.util.Map;

import org.jbake.app.ContentStore;


/**
 * 
 * @author ndx
 *
 * @param Type the type of data returned by this model extractor
 */
public interface ModelExtractor<Type extends Object> {

	Type get(ContentStore db, Map model, String key);

}