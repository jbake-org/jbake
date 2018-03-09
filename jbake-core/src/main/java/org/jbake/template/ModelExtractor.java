package org.jbake.template;

import java.util.Map;

import org.jbake.app.ContentStore;


/**
 *
 * @author ndx
 *
 * @param <T> the type of data returned by this model extractor
 */
public interface ModelExtractor<T> {

	T get(ContentStore db, Map model, String key);

}