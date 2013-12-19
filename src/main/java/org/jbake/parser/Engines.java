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
package org.jbake.parser;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p>A singleton class giving access to markup engines. Markup engines are loaded based on classpath.
 * New engines may be registered either at runtime (not recommanded) or by putting a descriptor file
 * on classpath (recommanded).</p>
 *
 * <p>The descriptor file must be found in <i>META-INF</i> directory and named
 * <i>org.jbake.parser.Engines.properties</i>. The format of the file is easy:</p>
 * <code>
 * org.jbake.parser.RawMarkupEngine=html<br>
 * org.jbake.parser.AsciidoctorEngine=ad,adoc,asciidoc<br>
 * org.jbake.parser.MarkdownEngine=md<br>
 * </code>
 * <p>where the key is the class of the engine (must extend {@link org.jbake.parser.MarkupEngine} and have a no-arg
 * constructor and the value is a comma-separated list of file extensions that this engine is capable of proceeding.</p>
 *
 * <p>Markup engines are singletons, so are typically used to initialize the underlying renderning engines. They
 * <b>must not</b> store specific information of a currently processed file (use {@link ParserContext the parser context}
 * for that).</p>
 *
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered. This allows
 * JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better
 * fit for embedding.
 *
 * @author Cédric Champeau
 *
 */
public class Engines {
    private final static Engines INSTANCE;

    static {
        INSTANCE = new Engines();
        loadEngines();
    }

    private final Map<String, MarkupEngine> parsers;

    public static MarkupEngine get(String fileExtension) {
        return INSTANCE.getEngine(fileExtension);
    }

    public static void register(String fileExtension, MarkupEngine engine) {
        INSTANCE.registerEngine(fileExtension, engine);
    }

    public static Set<String> getRecognizedExtensions() {
        return Collections.unmodifiableSet(INSTANCE.parsers.keySet());
    }

    private Engines() {
        parsers = new HashMap<String, MarkupEngine>();
    }

    private void registerEngine(String fileExtension, MarkupEngine markupEngine) {
        MarkupEngine old = parsers.put(fileExtension, markupEngine);
        if (old != null) {
            System.out.println("Registered a markup engine for extension [." + fileExtension + "] but another one was already defined:" + old);
        }
    }

    private MarkupEngine getEngine(String fileExtension) {
        return parsers.get(fileExtension);
    }

    /**
     * This method is used to search for a specific class, telling if loading the engine would succeed. This is
     * typically used to avoid loading optional modules.
     *
     * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.
     * @return null if the engine is not available, an instance of the engine otherwise
     */
    private static MarkupEngine tryLoadEngine(String engineClassName) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends MarkupEngine> engineClass = (Class<? extends MarkupEngine>) Class.forName(engineClassName, false, Engines.class.getClassLoader());
            return engineClass.newInstance();
        } catch (ClassNotFoundException e) {
            return new ErrorEngine(engineClassName);
        } catch (InstantiationException e) {
            return new ErrorEngine(engineClassName);
        } catch (IllegalAccessException e) {
            return new ErrorEngine(engineClassName);
        } catch (NoClassDefFoundError e) {
            // a dependency of the engine may not be found on classpath
            return new ErrorEngine(engineClassName);
        }
    }

    /**
     * This method is used internally to load markup engines. Markup engines are found using descriptor files on classpath, so
     * adding an engine is as easy as adding a jar on classpath with the descriptor file included.
     */
    private static void loadEngines() {
        try {
            ClassLoader cl = Engines.class.getClassLoader();
            Enumeration<URL> resources = cl.getResources("META-INF/org.jbake.parser.Engines.properties");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Properties props = new Properties();
                props.load(url.openStream());
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    String className = (String) entry.getKey();
                    String[] extensions = ((String)entry.getValue()).split(",");
                    registerEngine(className, extensions);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerEngine(String className, String... extensions) {
        MarkupEngine engine = tryLoadEngine(className);
        if (engine != null) {
            for (String extension : extensions) {
                register(extension, engine);
            }
        }
    }
}
