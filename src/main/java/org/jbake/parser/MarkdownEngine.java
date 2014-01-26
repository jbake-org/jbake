/*
 * Copyright 2003-2012 the original author or authors.
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
package org.jbake.parser;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * Renders documents in the Markdown format.
 *
 * @author Cédric Champeau
 */
public class MarkdownEngine extends MarkupEngine {

    @Override
    public void processBody(final ParserContext context) {
        String[] mdExts = context.getConfig().getStringArray("markdown.extensions");

        int extensions = Extensions.NONE;
        if (mdExts.length > 0) {
            for (int index = 0; index < mdExts.length; index++) {
                if (mdExts[index].equals("HARDWRAPS")) {
                    extensions |= Extensions.HARDWRAPS;
                } else if (mdExts[index].equals("AUTOLINKS")) {
                    extensions |= Extensions.AUTOLINKS;
                } else if (mdExts[index].equals("FENCED_CODE_BLOCKS")) {
                    extensions |= Extensions.FENCED_CODE_BLOCKS;
                } else if (mdExts[index].equals("DEFINITIONS")) {
                    extensions |= Extensions.DEFINITIONS;
                } else if (mdExts[index].equals("ABBREVIATIONS")) {
                    extensions |= Extensions.ABBREVIATIONS;
                } else if (mdExts[index].equals("QUOTES")) {
                    extensions |= Extensions.QUOTES;
                } else if (mdExts[index].equals("SMARTS")) {
                    extensions |= Extensions.SMARTS;
                } else if (mdExts[index].equals("SMARTYPANTS")) {
                    extensions |= Extensions.SMARTYPANTS;
                } else if (mdExts[index].equals("SUPPRESS_ALL_HTML")) {
                    extensions |= Extensions.SUPPRESS_ALL_HTML;
                } else if (mdExts[index].equals("SUPPRESS_HTML_BLOCKS")) {
                    extensions |= Extensions.SUPPRESS_HTML_BLOCKS;
                } else if (mdExts[index].equals("SUPPRESS_INLINE_HTML")) {
                    extensions |= Extensions.SUPPRESS_INLINE_HTML;
                } else if (mdExts[index].equals("TABLES")) {
                    extensions |= Extensions.TABLES;
                } else if (mdExts[index].equals("WIKILINKS")) {
                    extensions |= Extensions.WIKILINKS;
                } else if (mdExts[index].equals("ALL")) {
                    extensions = Extensions.ALL;
                }
            }

        }
        PegDownProcessor pegdownProcessor = new PegDownProcessor(extensions);
        context.setBody(pegdownProcessor.markdownToHtml(context.getBody()));
    }
}
