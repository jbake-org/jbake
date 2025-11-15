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
    @CommandLine.Parameters(
        index = "0",
        description = ["source folder of site content (with templates and assets), if not supplied will default to current directory"],
        arity = "0..1"
    )
    val sourceValue: String? = null

    @CommandLine.Parameters(
        index = "1",
        description = ["destination folder for output, if not supplied will default to a folder called \"output\" in the current directory"],
        arity = "0..1"
    )
    val destinationValue: String? = null

    @CommandLine.Option(names = ["-b", "--bake"], description = ["performs a bake"])
    val isBake: Boolean = false
        get() = field || (this.sourceValue != null && this.destinationValue != null)

    @CommandLine.ArgGroup(exclusive = false, heading = "%n%nJBake initialization%n%n")
    private val initGroup: InitOptions? = null

    @CommandLine.Option(
        names = ["-s", "--server"],
        description = ["runs HTTP server to serve out baked site, if no <value> is supplied will default to a folder called \"output\" in the current directory"]
    )
    val isRunServer: Boolean = false

    @CommandLine.Option(names = ["-h", "--help"], description = ["prints this message"], usageHelp = true)
    private val helpRequested = false

    @CommandLine.Option(names = ["--reset"], description = ["clears the local cache, enforcing rendering from scratch"])
    val isClearCache: Boolean = false

    @CommandLine.Option(
        names = ["-c", "--config"],
        description = ["use specified file for configuration (defaults to " + ConfigUtil.CONFIG_FILE + " in the source folder if not supplied)"]
    )
    val configValue: String? = null

    @JvmField
    @CommandLine.Option(
        names = ["--prop-encoding"],
        description = ["use given encoding to load properties file. default: utf-8"]
    )
    val propertiesEncoding: String = "utf-8"

    @CommandLine.Option(names = ["-ls", "--list-settings"], description = ["list configuration settings"])
    val isListConfig: Boolean = false

    val template: String?
        get() = initGroup.template

    fun getSource(): File {
        if (this.sourceValue != null) {
            return File(this.sourceValue)
        } else {
            return File(System.getProperty("user.dir"))
        }
    }

    fun getDestination(): File {
        if (this.destinationValue != null) {
            return File(this.destinationValue)
        } else {
            return File(getSource(), "output")
        }
    }

    fun getConfig(): File {
        if (this.configValue != null) {
            return File(this.configValue)
        } else {
            return File(getSource(), "jbake.properties")
        }
    }

    val isHelpNeeded: Boolean
        get() = helpRequested || !(this.isListConfig || this.isBake || this.isRunServer || this.isInit || this.sourceValue != null || this.destinationValue != null)

    val isInit: Boolean
        get() = (initGroup != null && initGroup.init)

    internal class InitOptions {
        @CommandLine.Option(
            names = ["-i", "--init"],
            paramLabel = "<target>",
            description = ["initialises required folder structure with default templates (defaults to current directory if <source> is not supplied)"],
            required = true
        )
        private val init = false

        @CommandLine.Option(
            names = ["-t", "--template"],
            defaultValue = "freemarker",
            fallbackValue = "freemarker",
            description = ["use specified template engine for default templates (uses Freemarker if <template> is not supplied) "],
            arity = "0..1"
        )
        private val template: String? = null
    }
}
