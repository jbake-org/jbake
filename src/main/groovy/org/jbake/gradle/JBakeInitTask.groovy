/*
 * Copyright 2014-2018 the original author or authors.
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
package org.jbake.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jbake.gradle.impl.JBakeInitProxyImpl

import java.lang.reflect.Constructor

class JBakeInitTask extends DefaultTask {
    @Input @Optional String template
    @Input @Optional String templateUrl
    @OutputDirectory File outputDir
    @Input Map<String, Object> configuration = [:]

    @Input @Optional
    Configuration classpath
    private static ClassLoader cl

    private JBakeInitProxy init

    JBakeInitTask() {
        group = 'Documentation'
        description = 'Initializes the directory structure for a new JBake site'
    }

    @TaskAction
    void init() {
        String _template = getTemplate()
        String _templateUrl = getTemplateUrl()

        if(!_template && _templateUrl) {
            throw new IllegalStateException("You must define a value for either 'template' or 'templateUrl")
        }

        createJBakeInit()
        init.prepare()
        mergeConfiguration()

        _templateUrl ? init.initFromTemplateUrl(_templateUrl, getOutputDir()) :
            init.initFromTemplate(_template, getOutputDir())
    }

    private JBakeInitProxy createJBakeInit() {
        if (!init) {
            loadInitDynamic().newInstance()
            init = new JBakeInitProxyImpl(delegate: loadInitDynamic())
        }
    }

    private mergeConfiguration() {
        def delegate = loadClass('org.apache.commons.configuration.CompositeConfiguration')
        Constructor constructor = delegate.getConstructor(loadClass('org.apache.commons.configuration.Configuration'))
        init.config = constructor.newInstance(createMapConfiguration())
    }

    private createMapConfiguration() {
        def delegate = loadClass('org.apache.commons.configuration.MapConfiguration')
        Constructor constructor = delegate.getConstructor(Map)
        constructor.newInstance(getConfiguration())
    }

    private loadInitDynamic() {
        setupClassLoader()
        loadClass('org.jbake.gradle.impl.Init')
    }

    private static Class loadClass(String className) {
        cl.loadClass(className)
    }

    private setupClassLoader() {
        if (classpath?.files) {
            def urls = classpath.files.collect { it.toURI().toURL() }
            cl = new URLClassLoader(urls as URL[], Thread.currentThread().contextClassLoader)
            Thread.currentThread().contextClassLoader = cl
        } else {
            cl = Thread.currentThread().contextClassLoader
        }
    }
}
