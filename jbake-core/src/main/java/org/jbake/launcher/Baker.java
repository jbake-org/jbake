package org.jbake.launcher;

import org.jbake.app.JBakeException;
import org.jbake.app.Oven;
import org.jbake.app.configuration.JBakeConfiguration;

import java.text.MessageFormat;
import java.util.List;

/**
 * Delegate class responsible for launching a Bake.
 *
 * @author jmcgarr@gmail.com
 */
public class Baker {

    public void bake(final JBakeConfiguration config) {
        final Oven oven = new Oven(config);
        oven.bake();

        final List<Throwable> errors = oven.getErrors();
        if (!errors.isEmpty()) {
            final StringBuilder msg = new StringBuilder();
            // TODO: decide, if we want the all errors here
            msg.append( MessageFormat.format("JBake failed with {0} errors:\n", errors.size()));
            int errNr = 1;
            for (final Throwable error : errors) {
                msg.append(MessageFormat.format("{0}. {1}\n", errNr, error.getMessage()));
                ++errNr;
            }
            throw new JBakeException(msg.toString(), errors.get(0));
        }
    }
}
