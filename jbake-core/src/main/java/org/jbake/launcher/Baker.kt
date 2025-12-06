package org.jbake.launcher

import org.jbake.app.JBakeExitException
import org.jbake.app.Oven
import org.jbake.app.SystemExit
import org.jbake.app.configuration.JBakeConfiguration

/**
 * A delegate class responsible for launching a Bake.
 */
class Baker {

    fun bake(config: JBakeConfiguration) {
        val oven = Oven(config)
        oven.bakeEverything()

        val errors = oven.errors
        if (errors.isEmpty()) return

        val msg = "JBake failed with ${errors.size} errors: " +
            errors.mapIndexed { index, error -> "\n  ${index + 1}. ${error.message}" }
                .joinToString()
        throw JBakeExitException(SystemExit.ERROR, msg, errors[0])
    }
}
