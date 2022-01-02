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
package org.jbake.gradle.impl

import org.gradle.api.logging.Logging
import org.gradle.workers.WorkAction
import org.jbake.app.Oven
import org.jbake.app.configuration.JBakeConfigurationFactory

abstract class JBakeWorkAction implements WorkAction<JBakeWorkActionParameters> {
    private static final LOGGER = Logging.getLogger(JBakeWorkAction)
    private Oven jbake

    @Override
    void execute() {
        def configuration = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(parameters.input.get().asFile, parameters.output.get().asFile, parameters.clearCache.get())
        parameters.configuration.get().each {
            configuration.setProperty(it.key, it.value)
        }
        jbake = new Oven(configuration)
        jbake.bake()
        List<Throwable> errors = jbake.getErrors()
        if (errors) {
            errors.each { LOGGER.error(it.message) }
            throw new IllegalStateException(errors.join('\n'))
        }
    }
}