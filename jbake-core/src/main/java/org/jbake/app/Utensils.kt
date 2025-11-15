package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration

/**
 * A helper class to wrap all the utensils that are needed to bake.
 */
class Utensils {
    @JvmField
    var configuration: JBakeConfiguration? = null
    @JvmField
    var contentStore: ContentStore? = null
    @JvmField
    var crawler: Crawler? = null
    @JvmField
    var renderer: Renderer? = null
    @JvmField
    var asset: Asset? = null
}

