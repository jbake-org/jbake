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

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

abstract class PluginIntegrationSpec extends Specification {

    @TempDir
    File tempDir
    
    File buildFile

    boolean debug = false
    String gradleVersion

    void setup() {

        buildFile = new File(tempDir,'build.gradle')
        File gradleProperties = new File(tempDir, 'gradle.properties')

        gradleProperties.withWriter {
            it.append("org.gradle.jvmargs=-XX:MaxDirectMemorySize=1g")
        }
    }

    protected void copyResources(String srcDir, String destination) {
        ClassLoader classLoader = getClass().getClassLoader()
        URL resource = classLoader.getResource(srcDir)
        if (resource == null) {
            throw new RuntimeException("Could not find classpath resource: $srcDir")
        }

        File destinationFile = new File(destination)
        File resourceFile = new File(resource.toURI())

        if (resourceFile.file) {
            FileUtils.copyFile(resourceFile, destinationFile)
        } else {
            FileUtils.copyDirectory(resourceFile, destinationFile)
        }
    }

    File newFolder(String s) {
        def dir = new File(tempDir, s)
        dir.mkdir()
        dir
    }

    File getProjectDir() {
        tempDir
    }

    protected GradleRunner getGradleRunner(String... arguments) {
        GradleRunner runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(arguments)
            .withPluginClasspath()
            .withDebug(debug)

        if (gradleVersion) {
            runner.withGradleVersion(gradleVersion)
        }
        runner
    }

    protected BuildResult runTasksWithSuccess(String... arguments) {
        getGradleRunner(arguments).build()
    }

    protected BuildResult runTasksWithFailure(String... arguments) {
        getGradleRunner(arguments).buildAndFail()
    }

    protected BuildResult runTasks(boolean success, String... arguments) {
        success ? runTasksWithSuccess(arguments) : runTasksWithFailure(arguments)
    }
}
