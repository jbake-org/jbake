/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 the original author or authors.
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

import org.jbake.gradle.JBakeProxy

import java.lang.reflect.Constructor

class JBakeProxyImpl implements JBakeProxy {
    Class delegate
    def input
    def output
    def clearCache

    def jbake

    def jbake() {
        if (jbake) {
            jbake.bake()
        }
    }

    def prepare() {
        Constructor constructor = delegate.getConstructor(File.class, File.class, boolean)

        jbake = constructor.newInstance(input, output, clearCache)
        jbake.setupPaths()
    }

    def getConfig() {
        jbake.config
    }

    def setConfig(Object config) {
        jbake.config = config
    }

    List<String> getErrors() {
        try {
            return jbake.getErrors()
        } catch (Exception e) {
            // older versions of the Oven class do not have a getErrors() method
            return []
        }
    }
}
