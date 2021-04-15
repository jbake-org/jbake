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

import org.gradle.testkit.runner.BuildResult
import org.junit.Assume
import spock.lang.Shared
import spock.lang.Unroll
import spock.util.environment.Jvm

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Unroll
class JbakeIntegrationSpec extends PluginIntegrationSpec {
    @Shared
    String latestGradleVersion = '7.0'

    @Shared
    String latestJbakeVersion = '2.6.6'

    @Unroll
    def 'Setup and bake with gradle #version'() {
        given:

        if (Jvm.current.java9Compatible) {
            Assume.assumeTrue("Skip. Not jdk9 compatible", jdk9Compatible)
        }
        if ((Jvm.current.javaSpecificationVersion as float) >= 14f) {
            Assume.assumeTrue("Skip. $gradleVersion is not jdk14 compatible", jdk14Compatible)
        }

        gradleVersion = version
        File jbakeSourceDir = newFolder('src', 'jbake')
        File jbakeDestinationDir = new File(projectDir, 'build/jbake')
        File blogTagFile = new File(jbakeDestinationDir, 'tags/blog.html')

        copyResources('example-project', jbakeSourceDir.path)

        buildFile << """
            plugins {
                id 'org.jbake.site'
            }

            jbake {
                version = '$latestJbakeVersion'
                configuration['render.tags'] = true
            }
        """

        when:
        BuildResult result = runTasksWithSucess('bake', '--info')

        then:
        result.task(':bake').outcome == SUCCESS
        result.output.contains('Baked 11 items')

        blogTagFile.size() > 0

        where:
        version             | jdk9Compatible | jdk14Compatible
        '5.0'               | true           | false            // base version
        '5.6.4'             | true           | false            // latest 5.x version
        '6.8.3'             | true           | true             // latest 6.x version
        latestGradleVersion | true           | true             // latest release, deprecations & warnings
    }

    def 'Bake with default repositories set to #includeDefaultRepositories results in #status'() {
        given:
        gradleVersion = latestGradleVersion
        File jbakeSourceDir = newFolder('src', 'jbake')

        copyResources('example-project', jbakeSourceDir.path)

        buildFile << """
            plugins {
                id 'org.jbake.site'
            }

            jbake {
                version = '$latestJbakeVersion'
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
                id 'org.jbake.site'
            }

            repositories {
                jcenter()
            }

            jbake {
                version = '$latestJbakeVersion'
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
