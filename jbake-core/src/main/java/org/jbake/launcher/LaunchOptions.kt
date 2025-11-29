package org.jbake.launcher

import org.jbake.app.configuration.ConfigUtil
import picocli.CommandLine
import java.io.File

@CommandLine.Command(
    description = ["JBake is a Java based, open source, static site/blog generator for developers & designers"],
    name = "jbake",
    usageHelpAutoWidth = true
)
class LaunchOptions {

    @CommandLine.Parameters(index = "0", arity = "0..1",
        description = ["source dir of site content (with templates and assets), if not supplied will default to current directory"],
    )
    var sourceValue: String? = null

    @CommandLine.Parameters(index = "1", arity = "0..1",
        description = ["destination dir for output, if not supplied will default to a dir called \"output\" in the current directory"],
    )
    var destinationValue: String? = null

    @CommandLine.Option(names = ["-b", "--bake"], description = ["performs a bake"])
    var isBake: Boolean = false
        get() = field || (this.sourceValue != null && this.destinationValue != null)

    @CommandLine.ArgGroup(exclusive = false, heading = "%n%nJBake initialization%n%n")
    private var initGroup: InitOptions? = null

    @CommandLine.Option(
        names = ["-s", "--server"],
        description = ["runs HTTP server to serve out baked site, if no <value> is supplied will default to a dir called \"output\" in the current directory"]
    )
    var isRunServer: Boolean = false

    @CommandLine.Option(names = ["-h", "--help"], description = ["prints this message"], usageHelp = true)
    private var helpRequested = false

    @CommandLine.Option(names = ["--reset"], description = ["clears the local cache, enforcing rendering from scratch"])
    var isClearCache: Boolean = false

    @CommandLine.Option(
        names = ["-c", "--config"],
        description = ["use specified file for configuration (defaults to " + ConfigUtil.CONFIG_FILE + " in the source dir if not supplied)"]
    )
    var configValue: String? = null

    @JvmField
    @CommandLine.Option(
        names = ["--prop-encoding"],
        description = ["use given encoding to load properties file. default: utf-8"]
    )
    var propertiesEncoding: String = "utf-8"

    @CommandLine.Option(names = ["-ls", "--list-settings"], description = ["list configuration settings"])
    var isListConfig: Boolean = false

    val template: String?
        get() = initGroup?.template

    fun getSource(): File {
        return sourceValue?.let { File(it) }
            ?: File(System.getProperty("user.dir"))
    }

    fun getDestination(): File {
        return destinationValue?.let { File(it) }
            ?: File(getSource(), "output")
    }

    fun getConfig(): File {
        return configValue?.let { File(it) }
            ?: File(getSource(), "jbake.properties")
    }

    val isHelpNeeded: Boolean
        get() = helpRequested || !(this.isListConfig || this.isBake || this.isRunServer || this.isInit || this.sourceValue != null || this.destinationValue != null)

    val isInit: Boolean
        get() {
            val group = initGroup
            return group != null && group.init
        }

    internal class InitOptions {
        @CommandLine.Option(
            names = ["-i", "--init"],
            paramLabel = "<target>",
            description = ["initialises required dir structure with default templates (defaults to current directory if <source> is not supplied)"],
            required = true
        )
        var init = false

        @CommandLine.Option(
            names = ["-t", "--template"],
            defaultValue = "freemarker",
            fallbackValue = "freemarker",
            description = ["use specified template engine for default templates (uses Freemarker if <template> is not supplied) "],
            arity = "0..1"
        )
        var template: String? = null
    }
}
