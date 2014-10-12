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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.project.ProjectInternal

class JBakePlugin implements Plugin<Project> {


    public static final String JBAKE = "jbake"

    void apply(Project project) {
        project.apply(plugin: 'base')

        JBakeExtension extension = project.extensions.create(JBAKE, JBakeExtension)

        project.repositories {
            jcenter()
        }

        Configuration configuration = project.configurations.maybeCreate(JBAKE)

        project.afterEvaluate{
            project.dependencies {
                jbake("org.jbake:jbake-core:${extension.version}")
                //TODO remove hard coded Engine-Versions to JBakeExtension
                //TODO jbake >= 2.3.1 switched to org.asciidoctor:asciidoctorj:1.5.+
                jbake("org.asciidoctor:asciidoctor-java-integration:0.1.4")
                jbake("org.freemarker:freemarker:2.3.19")
                jbake("org.pegdown:pegdown:1.4.2")
            }
        }

        project.task('jbake', type: JBakeTask, group: 'Documentation', description: 'Bake a jbake project'){
            classpath = configuration
        }

    }
}
