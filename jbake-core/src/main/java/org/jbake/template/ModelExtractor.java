package org.jbake.template;

import org.jbake.app.ContentStore;

import java.util.Map;


/**
 *
 * @author ndx
 *
 * @param <T> the type of data returned by this model extractor
 */
public interface ModelExtractor<T> {

	T get(ContentStore db, Map model, String key);

}