package org.jbake.launcher

import org.jbake.app.JBakeException
import org.jbake.app.Oven
import org.jbake.app.SystemExit
import org.jbake.app.configuration.JBakeConfiguration
import java.text.MessageFormat

/**
 * Delegate class responsible for launching a Bake.
 */
class Baker {

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
