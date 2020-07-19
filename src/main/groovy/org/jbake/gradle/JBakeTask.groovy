/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 the original author or authors.
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
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.util.GradleVersion
import org.gradle.workers.WorkerExecutor
import org.jbake.gradle.impl.JBakeProxyImpl
import org.jbake.gradle.impl.JBakeWorkAction

import javax.inject.Inject
import java.lang.reflect.Constructor

class JBakeTask extends DefaultTask {
    @InputDirectory File input
    @OutputDirectory File output
    @Input Map<String, Object> configuration = [:]
    @Input
    boolean clearCache

    @Classpath @Optional
    Configuration classpath
    private static ClassLoader cl

    private JBakeProxy jbake

    private final WorkerExecutor executor

    @Inject
    JBakeTask(WorkerExecutor executor) {
        group = 'jbake'
        description = 'Bakes your website with JBake'
        this.executor = executor
    }

    @TaskAction
    void bake() {
        // Newer worker API was introduced in 5.6, older worker API doesn't properly forward the logging from Oven which would cause a regression.
        // See https://github.com/gradle/gradle/issues/8318
        if (GradleVersion.current().compareTo(GradleVersion.version("5.6")) >= 0) {
            executor.classLoaderIsolation {
                it.classpath.from(this.classpath)
            }.submit(JBakeWorkAction) {
                it.input = this.input
                it.output = this.output
                it.clearCache = this.clearCache
                it.configuration.putAll(this.configuration)
            }
        } else {
            createJbake()
            jbake.prepare()
            mergeConfiguration()
            jbake.jbake()
            List<String> errors = jbake.getErrors()
            if (errors) {
                errors.each { logger.error(it) }
                throw new TaskExecutionException(this, new IllegalStateException(errors.join('\n')))
            }
        }
    }

    private JBakeProxy createJbake() {
        if (!jbake) {
            jbake = new JBakeProxyImpl(delegate: loadOvenDynamic(), input: getInput(), output: getOutput(), clearCache: getClearCache())
        }
    }

    private mergeConfiguration() {
        def delegate = loadClass('org.apache.commons.configuration.CompositeConfiguration')
        Constructor constructor = delegate.getConstructor(Collection)
        def config = constructor.newInstance([createMapConfiguration(), jbake.getConfig()])
        jbake.setConfig(config)
    }

    private createMapConfiguration() {
        def delegate = loadClass('org.apache.commons.configuration.MapConfiguration')
        Constructor constructor = delegate.getConstructor(Map)
        constructor.newInstance(getConfiguration())
    }

    private loadOvenDynamic() {
        setupClassLoader()
        loadClass('org.jbake.app.Oven')
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
