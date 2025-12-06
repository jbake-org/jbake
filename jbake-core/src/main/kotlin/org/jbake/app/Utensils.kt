package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationInspector


/** A helper class to wrap all the utensils that are needed to bake. */
class Utensils(
    val configuration: JBakeConfiguration,
    val contentStore: ContentStore,
    val crawler: Crawler,
    val renderer: Renderer,
    val asset: Asset
)



/**
 * A factory to create a [Utensils] object.
 */
object UtensilsFactory {
    /**
     * Create default [Utensils] by a given [JBakeConfiguration]
     * @return a default [Utensils] instance
     */
    fun createDefaultUtensils(config: JBakeConfiguration): Utensils {
        val inspector = JBakeConfigurationInspector(config)
        inspector.inspect()

        val contentStore = DbUtils.createDataStore(config)
        return Utensils(
            configuration = config,
            contentStore = contentStore,
            crawler = Crawler(contentStore, config),
            renderer = Renderer(contentStore, config),
            asset = Asset(config)
        )
    }
}
