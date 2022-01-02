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
import org.gradle.api.tasks.*
import org.gradle.util.GradleVersion
import org.gradle.workers.WorkerExecutor
import org.jbake.gradle.impl.JBakeWorkAction

import javax.inject.Inject

class JBakeTask extends DefaultTask {
    @InputDirectory File input
    @OutputDirectory File output
    @Input Map<String, Object> configuration = [:]
    @Input
    boolean clearCache

    @Classpath @Optional
    Configuration classpath

    private final WorkerExecutor executor

    @Inject
    JBakeTask(WorkerExecutor executor) {
        group = 'jbake'
        description = 'Bakes your website with JBake'
        this.executor = executor
    }

    @TaskAction
    void bake() {
        executor.classLoaderIsolation {
            it.classpath.from(this.classpath)
        }.submit(JBakeWorkAction) {
            it.input = this.input
            it.output = this.output
            it.clearCache = this.clearCache
            it.configuration.putAll(this.configuration)
        }
    }

}
