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

import org.gradle.workers.WorkAction
import org.jbake.app.configuration.JBakeConfigurationFactory

abstract class JBakeInitAction implements WorkAction<JBakeInitActionParameters> {

    Init init = new Init()

    @Override
    void execute() {
        def configuration = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(
                parameters.output.get().asFile,
                parameters.output.get().asFile,
                true
        )
        parameters.configuration.get().each {
            configuration.setProperty(it.key, it.value)
        }
        
        init.config = configuration

        if (parameters.templateUrl.isPresent()) {
            initFromTemplateUrl()
        } else {
            initFromTemplate()
        }
    }

    void initFromTemplate() {
        try {
            def outputDir = parameters.output.get().asFile
            def type = parameters.template.get()
            init.run(outputDir, type)
            println("Base folder structure successfully created.")
        } catch (final Exception e) {
            final String msg = "Failed to initialise structure: " + e.getMessage()
            throw new IllegalStateException(msg, e)
        }
    }

    void initFromTemplateUrl() {
        try {
            def outputDir = parameters.output.get().asFile
            def templateUrl = new URL(parameters.templateUrl.get())
            init.run(outputDir, templateUrl)
            println("Base folder structure successfully created.")
        } catch (final Exception e) {
            final String msg = "Failed to initialise structure: " + e.getMessage()
            throw new IllegalStateException(msg, e)
        }
    }

}