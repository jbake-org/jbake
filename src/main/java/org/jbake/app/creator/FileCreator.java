package org.jbake.app.creator;

import java.util.List;

public interface FileCreator {
    boolean accepts(String fileName);
    List<String> obtainMetadata();
    String getSeparator();
}
