package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration

/**
 * A helper class to wrap all the utensils that are needed to bake.
 */
class Utensils(
    val configuration: JBakeConfiguration,
    val contentStore: ContentStore,
    val crawler: Crawler,
    val renderer: Renderer,
    val asset: Asset
)

