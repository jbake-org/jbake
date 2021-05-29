package org.jbake.template;

import org.jbake.app.ContentStore;
import org.jbake.template.model.TemplateModel;


/**
 * @param <T> the type of data returned by this model extractor
 * @author ndx
 */
public interface ModelExtractor<T> {

    T get(ContentStore db, TemplateModel model, String key);

}
