package org.jbake.launcher

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.JBakeException
import org.jbake.app.Oven
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import java.text.MessageFormat

/**
 * Delegate class responsible for launching a Bake.
 */
class Baker {
    /**
     * @param options The given cli options
     */
    @Deprecated("use {@link Baker#bake(JBakeConfiguration)} instead")
    fun bake(options: LaunchOptions, config: CompositeConfiguration) {
        val configuration: JBakeConfiguration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(
            options.getSource(),
            options.getDestination(),
            config,
            options.isClearCache
        )
        bake(configuration)
    }

    fun bake(config: JBakeConfiguration) {
        val oven = Oven(config)
        oven.bakeEverything()

        val errors = oven.errors
        if (!errors.isEmpty()) {
            val msg = StringBuilder()
            // TODO: Decide if we want all errors here.
            msg.append(MessageFormat.format("JBake failed with {0} errors:\n", errors.size))
            var errNr = 1
            for (error in errors) {
                msg.append(MessageFormat.format("{0}. {1}\n", errNr, error.message))
                ++errNr
            }
            throw JBakeException(SystemExit.ERROR, msg.toString(), errors[0])
        }
    }
}
