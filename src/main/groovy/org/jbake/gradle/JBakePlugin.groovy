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

import com.github.zafarkhaja.semver.Version
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePlugin

@CompileStatic
class JBakePlugin implements Plugin<Project> {
    static final String JBAKE = 'jbake'

    Project project

    void apply(Project project) {
        this.project = project
        project.pluginManager.apply(BasePlugin)

        Configuration configuration = project.configurations.maybeCreate(JBAKE)
        JBakeExtension jbakeExtension = project.extensions.create(JBAKE, JBakeExtension)

        addDependenciesAfterEvaluate()

        project.tasks.register('bake', JBakeTask, new Action<JBakeTask>() {
            @Override
            void execute(JBakeTask t) {
                t.group = 'Documentation'
                t.description = 'Bake a jbake project'
                t.classpath = configuration
                t.input = project.layout.projectDirectory.file(jbakeExtension.srcDirName).asFile
                t.output = project.layout.buildDirectory.dir(jbakeExtension.destDirName).get().asFile
                t.clearCache = jbakeExtension.clearCache
                t.configuration = jbakeExtension.configuration
            }
        })

        project.tasks.register('bakePreview', JBakeServeTask, new Action<JBakeServeTask>() {
            @Override
            void execute(JBakeServeTask t) {
                t.group = 'Documentation'
                t.description = 'Preview a jbake project'
                t.classpath = configuration
                t.input = project.layout.buildDirectory.file(jbakeExtension.destDirName).get().asFile
                t.configuration = jbakeExtension.configuration
            }
        })

        project.tasks.register('bakeInit', JBakeInitTask, new Action<JBakeInitTask>() {
            @Override
            void execute(JBakeInitTask t) {
                t.group = 'Documentation'
                t.description = 'Setup a jbake project'
                t.template = jbakeExtension.template
                t.templateUrl = jbakeExtension.templateUrl
                t.outputDir = project.layout.projectDirectory.file(jbakeExtension.srcDirName).asFile
                t.configuration = jbakeExtension.configuration
            }
        })
    }

    private void addDependenciesAfterEvaluate() {
        project.afterEvaluate {
            JBakeExtension jbakeExtension = project.extensions.findByType(JBakeExtension)
            if (jbakeExtension.includeDefaultRepositories) {
                project.repositories.jcenter()
            }

            addDependencies()
        }
    }

    @CompileDynamic
    private void addDependencies() {
        JBakeExtension jbakeExtension = project.extensions.findByType(JBakeExtension)
        project.dependencies {
            jbake("org.jbake:jbake-core:${jbakeExtension.version}")

            Version currentVersion = Version.valueOf(jbakeExtension.version)
            Version jbake2_3_0 = Version.valueOf('2.3.0')
            Version jbake2_6_0 = Version.valueOf('2.6.0')

            if (currentVersion.greaterThan(jbake2_3_0)) {
                jbake("org.asciidoctor:asciidoctorj:${jbakeExtension.asciidoctorjVersion}")
            } else {
                jbake("org.asciidoctor:asciidoctor-java-integration:${jbakeExtension.asciidoctorJavaIntegrationVersion}")
            }

            if (currentVersion.greaterThanOrEqualTo(jbake2_6_0)) {
                jbake("com.vladsch.flexmark:flexmark:${jbakeExtension.flexmarkVersion}")
                jbake("com.vladsch.flexmark:flexmark-profile-pegdown:${jbakeExtension.flexmarkVersion}")
            } else {
                jbake("org.pegdown:pegdown:${jbakeExtension.pegdownVersion}")
            }

            jbake("org.freemarker:freemarker:${jbakeExtension.freemarkerVersion}")
            jbake("org.codehaus.groovy:groovy:${jbakeExtension.groovyTemplatesVersion}")
            jbake("org.codehaus.groovy:groovy-templates:${jbakeExtension.groovyTemplatesVersion}")
            jbake("de.neuland-bfi:jade4j:${jbakeExtension.jade4jVersion}")
            jbake("org.thymeleaf:thymeleaf:${jbakeExtension.thymeleafVersion}")
            jbake("org.pegdown:pegdown:${jbakeExtension.pegdownVersion}")
            jbake("org.eclipse.jetty:jetty-server:${jbakeExtension.jettyVersion}")
        }
    }
}
