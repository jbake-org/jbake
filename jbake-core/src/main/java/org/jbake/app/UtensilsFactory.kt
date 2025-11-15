package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationInspector

/**
 * A factory to create a [Utensils] object
 */
object UtensilsFactory {
    /**
     * Create default [Utensils] by a given [JBakeConfiguration]
     * @param config a [JBakeConfiguration]
     * @return a default [Utensils] instance
     */
    fun createDefaultUtensils(config: JBakeConfiguration): Utensils {
        val inspector = JBakeConfigurationInspector(config)
        inspector.inspect()

        val utensils = Utensils()
        utensils.setConfiguration(config)
        val contentStore = DBUtil.createDataStore(config)
        utensils.setContentStore(contentStore)
        utensils.setCrawler(Crawler(contentStore, config))
        utensils.setRenderer(Renderer(contentStore, config))
        utensils.setAsset(Asset(config))

        return utensils
    }
}
