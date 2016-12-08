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

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

class JbakeIntegrationSpec extends PluginIntegrationSpec {

    @Unroll
    def "setup and bake with gradle #version"() {
        given:
        gradleVersion = version
        File jbakeSourceDir = newFolder("src", "jbake")
        File jbakeDestinationDir = new File(projectDir,"build/jbake")
        File blogTagFile = new File(jbakeDestinationDir,"tags/blog.html")

        copyResources("example-project", jbakeSourceDir.path)

        buildFile << '''
            plugins {
                id 'me.champeau.jbake'
            }

            jbake{
                asciidoctorjVersion = "1.5.4.1"
                version = '2.4.0'
                configuration['render.tags'] = true
            }
        '''

        when:
        BuildResult result = runTasksWithSuccess("bake", "--info")

        then:
        result.task(":bake").outcome == SUCCESS
        result.output.contains("Baked 5 items")

        blogTagFile.size() > 0

        where:
        version << ['2.8','2.14.1','3.0']
    }


}
