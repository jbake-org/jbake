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
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jbake.gradle.impl.JettyServerProxyImpl

class JBakeServeTask extends DefaultTask {
    @InputDirectory File input
    @Input Map<String, Object> configuration = [:]
    @Input String port = '8080'

    @Classpath @Optional
    Configuration classpath
    private static ClassLoader cl


    private JettyServerProxy jettyServer

    JBakeServeTask() {
        group = 'Documentation'
        description = 'Starts up a Jetty container to preview your JBake site locally.'
    }

    @TaskAction
    void serve() {
        logging.level = LogLevel.INFO
        createJettyServer()
        jettyServer.prepare()
        println("Starting server. Browse to http://localhost:${getPort()}")
        jettyServer.run(getInput().absolutePath, getPort())
    }

    private JettyServerProxy createJettyServer() {
        if (!jettyServer) {
            jettyServer = new JettyServerProxyImpl(delegate: loadJettyServerDynamic())
        }
    }

    private loadJettyServerDynamic() {
        setupClassLoader()
        loadClass('org.jbake.launcher.JettyServer')
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
