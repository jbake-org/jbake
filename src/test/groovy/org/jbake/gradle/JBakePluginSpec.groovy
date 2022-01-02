/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2021 the original author or authors.
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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class JBakePluginSpec extends Specification {
    public static final String PLUGIN_ID = 'org.jbake.site'

    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: PLUGIN_ID
    }

    def "should add a JBakeTask"() {
        expect:
        project.tasks.bake instanceof JBakeTask
    }

    def "should add a JBakeInitTask"() {
        expect:
        project.tasks.bakeInit instanceof JBakeInitTask
    }

    def "should add a JBakeServeTask"() {
        expect:
        project.tasks.bakePreview instanceof JBakeServeTask
    }

    def "should add jbake configuration"() {
        expect:
        project.configurations.jbake
    }

    def "should define default jbake version"() {
        expect:
        project.jbake.version != null
    }

    @Unroll
    def "should add dependency #name #version"() {
        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.name == name && it.version == version
        }

        where:
        group                   | name                          | version
        'org.jbake'             | 'jbake-core'                  | "${new JBakeExtension().version}"
        'org.freemarker'        | 'freemarker'                  | "${new JBakeExtension().freemarkerVersion}"
        'com.vladsch.flexmark'  | 'flexmark'                    | "${new JBakeExtension().flexmarkVersion}"
        'com.vladsch.flexmark'  | 'flexmark-profile-pegdown'    | "${new JBakeExtension().flexmarkVersion}"
        'org.asciidoctor'       | 'asciidoctorj'                | "${new JBakeExtension().asciidoctorjVersion}"
        'org.codehaus.groovy'   | 'groovy-templates'            | "${new JBakeExtension().groovyTemplatesVersion}"
        'org.thymeleaf'         | 'thymeleaf'                   | "${new JBakeExtension().thymeleafVersion}"
        'de.neuland-bfi'        | 'jade4j'                      | "${new JBakeExtension().jade4jVersion}"
        'io.pebbletemplates'    | 'pebble'                      | "${new JBakeExtension().pebbleVersion}"
    }

    def "set dependency version by extension"() {
        given:
        project.jbake.version = '2.3.0'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.name == 'jbake-core' && it.version == '2.3.0'
        }
    }

    def "switch to asciidoctorj if version > 2.3.0"() {
        given:
        project.jbake.version = '2.3.1'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.group == 'org.asciidoctor' && it.name == 'asciidoctorj'
        }
    }

    def "use asciidoctor-java-integration if version < 2.3.1"() {
        given:
        project.jbake.version = '2.3.0'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.group == 'org.asciidoctor' &&
                it.name == 'asciidoctor-java-integration' &&
                it.version == '0.1.4'
        }
    }

    def "switch to flexmark if version >= 2.6.0"() {
        given:
        project.jbake.version = '2.6.0'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.group == 'com.vladsch.flexmark' && it.name == 'flexmark'
        }
    }

    def "use pegdown if version is < 2.6.0"() {
        given:
        project.jbake.version = '2.5.1'

        when:
        project.evaluate()

        then:
        project.configurations.jbake.dependencies.find {
            it.group == 'org.pegdown' && it.name == 'pegdown'
        }
    }

    def "input dir should be configured by extension"() {
        given:
        def srcDirName = "src/jbake-project"
        def expectedFile = project.file("$project.rootDir/$srcDirName")

        when:
        project.jbake.srcDirName = srcDirName

        then:
        project.tasks.bake.input == expectedFile
    }

    def "output dir should be configured by extension"() {
        given:
        def destDirName = "jbake-out"
        def expectedFile = project.file("$project.buildDir/$destDirName")

        when:
        project.jbake.destDirName = destDirName

        then:
        project.tasks.bake.output == expectedFile
    }

    def "clearcache should be configured by extension"() {
        given:
        def clearCache = true

        when:
        project.jbake.clearCache = clearCache

        then:
        project.tasks.bake.clearCache == clearCache
    }

    def "should be configurable by extension"() {
        given:
        def configuration = [:]
        configuration['render.tags'] = false

        when:
        project.jbake.configuration = configuration

        then:
        project.tasks.bake.configuration['render.tags'] == false
    }
}
