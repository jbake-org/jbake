package org.jbake.render;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentIterator;
import org.jbake.app.Renderer;
import org.jbake.model.DocumentTypes;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class DocumentsRenderer implements RenderingTool {

	@Override
	public int render(Renderer renderer, ODatabaseDocumentTx db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
		int renderedCount = 0;
    	final List<String> errors = new LinkedList<String>();
		for (String docType : DocumentTypes.getDocumentTypes()) {
			DocumentIterator pagesIt = DBUtil.fetchDocuments(db, "select * from " + docType + " where rendered=false");
			while (pagesIt.hasNext()) {
				Map<String, Object> page = pagesIt.next();
				try {
					renderer.render(page);
					renderedCount++;
				} catch (Exception e) {
					errors.add(e.getMessage());
				}
			}
		}
        if (!errors.isEmpty()) {
        	StringBuilder sb = new StringBuilder();
        	sb.append("Failed to render documents. Cause(s):");
        	for(String error: errors) {
        		sb.append("\n" + error);
        	}
        	throw new RenderingException(sb.toString());
        } else {
        	return renderedCount;
        }
	}
}
