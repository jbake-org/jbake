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

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.MapConfiguration
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import org.jbake.launcher.JettyServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JBakePreviewTask extends AbstractTask {

    @InputDirectory File input = new File("$project.buildDir/jbake")

    // TODO should be loaded from jbake.properties
    @Input String port = "8820"

    @TaskAction
    void jbakePreview() {
        // TODO set logging level for the JettyServer instead
        println "You can preview your site at: http://localhost:$port"
        Logger log = LogFactory.getLogger
        JettyServer.run( input.absolutePath, port )
    }
}
