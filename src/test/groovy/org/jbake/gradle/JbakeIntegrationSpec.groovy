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

import org.assertj.core.api.Assumptions
import org.gradle.testkit.runner.BuildResult
import spock.lang.Shared
import spock.lang.Unroll
import spock.util.environment.Jvm

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Unroll
class JbakeIntegrationSpec extends PluginIntegrationSpec {
    @Shared
    String latestGradleVersion = '7.0.2'

    @Shared
    String latestJbakeVersion = '2.6.7'

    @Unroll
    def 'Setup and bake with gradle #version'() {
        given:

        if (Jvm.current.java9Compatible) {
            Assumptions.assumeThat(jdk9Compatible).isTrue()
        }
        if ((Jvm.current.javaSpecificationVersion as float) >= 14f) {
            Assumptions.assumeThat(jdk14Compatible).isTrue()
        }

        gradleVersion = version
        File jbakeSourceDir = newFolder('src/jbake')
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
        BuildResult result = runTasksWithSuccess('bake', '--info')

        then:
        result.task(':bake').outcome == SUCCESS
        result.output.contains('Baked 11 items')

        blogTagFile.size() > 0

        where:
        version             | jdk9Compatible | jdk14Compatible
        '5.6.4'             | true           | false            // latest 5.x version
        '6.8.3'             | true           | true             // latest 6.x version
        latestGradleVersion | true           | true             // latest release, deprecations & warnings
    }

    def 'Bake with default repositories set to #includeDefaultRepositories results in #status'() {
        given:
        gradleVersion = latestGradleVersion
        File jbakeSourceDir = newFolder('src/jbake')

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
        File jbakeSourceDir = newFolder('src/jbake')

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
        BuildResult result = runTasksWithSuccess('bake', '--info')

        then:
        result.task(':bake').outcome == SUCCESS
    }

    def "should show an error message using gradle <= 5.6"() {
        given:
        gradleVersion = "5.0"

        buildFile << """
            plugins {
                id 'org.jbake.site'
            }
        """

        when:
        BuildResult result = runTasksWithFailure('bake', '--info')

        then:
        result.output.contains("This plugin does not support gradle versions <= 5.6")
    }

    def "should initialize with example project"() {
        given:
        buildFile << """
            plugins {
                id 'org.jbake.site'
            }
        """
        !new File(tempDir, "src/jbake/templates").exists()
        !new File(tempDir, "src/jbake/assets").exists()
        !new File(tempDir, "src/jbake/content").exists()


        when:
        BuildResult result = runTasksWithSuccess('bakeInit')

        then:
        result.output.contains("Base folder structure successfully created.")
        new File(tempDir, "src/jbake/templates").exists()
        new File(tempDir, "src/jbake/assets").exists()
        new File(tempDir, "src/jbake/content").exists()
    }

    def "should initialize with from given zip url"() {
        given:
        buildFile << """
            plugins {
                id 'org.jbake.site'
            }
            
            jbake {
                templateUrl = 'https://github.com/jbake-org/jbake-example-project-groovy-mte/archive/master.zip'
            }
        """
        !new File(tempDir, "src/jbake/templates").exists()
        !new File(tempDir, "src/jbake/assets").exists()
        !new File(tempDir, "src/jbake/content").exists()


        when:
        BuildResult result = runTasksWithSuccess('bakeInit')

        then:
        result.output.contains("Base folder structure successfully created.")
        new File(tempDir, "src/jbake/templates/page.tpl").exists()
        new File(tempDir, "src/jbake/assets").exists()
        new File(tempDir, "src/jbake/content").exists()
    }
}
