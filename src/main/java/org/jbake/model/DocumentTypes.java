/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbake.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility class used to determine the list of document types. Currently only supports "page", "post", "index",
 * "archive" and "feed".
 * <p/>
 * Additional document types are added at runtime based on the types found in the configuration.
 *
 * @author Cédric Champeau
 */
public class DocumentTypes {

    private static final Set<String> DEFAULT_DOC_TYPES = new LinkedHashSet<String>(Arrays.asList("page", "post", "index", "archive", "feed"));

    public static void addDocumentType(String docType) {
        DEFAULT_DOC_TYPES.add(docType);
    }

    public static String[] getDocumentTypes() {
        return DEFAULT_DOC_TYPES.toArray(new String[DEFAULT_DOC_TYPES.size()]);
    }
}
