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

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import org.jbake.gradle.impl.JBakeInitAction

import javax.inject.Inject

class JBakeInitTask extends DefaultTask {
    @Input @Optional String template
    @Input @Optional String templateUrl
    @OutputDirectory File outputDir
    @Input Map<String, Object> configuration = [:]

    @Classpath @Optional
    Configuration classpath

    private final WorkerExecutor executor

    @Inject
    JBakeInitTask(WorkerExecutor executor) {
        group = 'Documentation'
        description = 'Initializes the directory structure for a new JBake site'
        this.executor = executor
    }

    @TaskAction
    void init() {
        if(!getTemplate() && getTemplateUrl()) {
            throw new IllegalStateException("You must define a value for either 'template' or 'templateUrl")
        }

        executor.classLoaderIsolation {
            it.classpath.from(this.classpath)
        }.submit(JBakeInitAction) {
            it.template = this.template
            it.templateUrl = this.templateUrl
            it.output = this.outputDir
            it.configuration.putAll(this.configuration)
        }
    }

}
