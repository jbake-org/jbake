/*
 * Copyright 2014 the original author or authors.
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
package me.champeau.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class JBakeTask extends DefaultTask {
    @InputDirectory File input
    @OutputDirectory File output
    @Input Map<String, Object> configuration = [:]
    boolean clearCache = false

    Configuration classpath
    private static ClassLoader cl

    JBakeProxy jbake

    @TaskAction
    void bake() {
        //TODO make it possible to change jbake configuration via extension?!
        createJbake()
        jbake.jbake()
    }

    private def createJbake() {
        if ( !jbake ) {
            jbake = new JBakeProxyImpl(delegate: loadOvenDynamic(), input: getInput(), output: getOutput(), clearCache: clearCache)
        }
    }

    private def loadOvenDynamic() {
        setupClassLoader()
        loadClass("org.jbake.app.Oven")
    }

    private static Class loadClass(String className) {
        cl.loadClass(className)
    }

    private def setupClassLoader() {
        if (classpath?.files) {
            def urls = classpath.files.collect { it.toURI().toURL() }
            cl = new URLClassLoader(urls as URL[], Thread.currentThread().contextClassLoader)
            Thread.currentThread().contextClassLoader = cl
        } else {
            cl = Thread.currentThread().contextClassLoader
        }
    }

}
