/*
 * Copyright 2014-2016 the original author or authors.
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

import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll
import spock.lang.Shared

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class JbakeIntegrationSpec extends PluginIntegrationSpec {

    @Shared
    def latestGradleVersion = '3.4.1'

    @Unroll
    def 'Setup and bake with gradle #version'() {
        given:
        gradleVersion = version
        File jbakeSourceDir = newFolder('src', 'jbake')
        File jbakeDestinationDir = new File(projectDir, 'build/jbake')
        File blogTagFile = new File(jbakeDestinationDir, 'tags/blog.html')

        copyResources('example-project', jbakeSourceDir.path)

        buildFile << '''
            plugins {
                id 'me.champeau.jbake'
            }

            jbake {
                asciidoctorjVersion = '1.5.4.1'
                version = '2.4.0'
                configuration['render.tags'] = true
            }
        '''

        when:
        BuildResult result = runTasksWithSucess('bake', '--info')

        then:
        result.task(':bake').outcome == SUCCESS
        result.output.contains('Baked 5 items')

        blogTagFile.size() > 0

        where:
        version << [
            '2.8',    // lower limit of Tooling API compatibility for TestKit
            '2.12',   // introduces changes such as compileOnly
            '2.14.1', // latest release in 2.x line
            '3.0',    // first release in 3.x line, compatibility changes
            latestGradleVersion     // latest release, deprecations & warnings
        ]
    }

    @Unroll
    def 'Bake with default repositories set to #includeDefaultRepositories results in #status'() {
        given:
        gradleVersion = latestGradleVersion
        File jbakeSourceDir = newFolder('src', 'jbake')

        copyResources('example-project', jbakeSourceDir.path)

        buildFile << """
            plugins {
                id 'me.champeau.jbake'
            }

            jbake {
                asciidoctorjVersion = '1.5.4.1'
                version = '2.4.0'
                includeDefaultRepositories = $includeDefaultRepositories
                configuration['render.tags'] = true
            }
        """

        when:
        BuildResult result = runTasks(status == SUCCESS, 'bake', '--info')

        then:
        result.task(':bake').outcome == status

        where:
        includeDefaultRepositories | status
        true                       | SUCCESS
        false                      | FAILED
    }

    def 'Bake with default repositories set to false and repositories block defined results in SUCCESS'() {
        given:
        gradleVersion = latestGradleVersion
        File jbakeSourceDir = newFolder('src', 'jbake')

        copyResources('example-project', jbakeSourceDir.path)

        buildFile << """
            plugins {
                id 'me.champeau.jbake'
            }

            repositories {
                jcenter()
            }

            jbake {
                asciidoctorjVersion = '1.5.4.1'
                version = '2.4.0'
                includeDefaultRepositories = false
                configuration['render.tags'] = true
            }
        """

        when:
        BuildResult result = runTasksWithSucess('bake', '--info')

        then:
        result.task(':bake').outcome == SUCCESS
    }
}
