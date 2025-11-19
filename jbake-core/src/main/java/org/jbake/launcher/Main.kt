package org.jbake.launcher

import org.jbake.app.FileUtil
import org.jbake.app.JBakeException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.util.ConfigurationPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import picocli.CommandLine
import java.io.File

/**
 * Launcher for JBake.
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
class Main @JvmOverloads constructor(
    private val baker: Baker = Baker(),
    private val jettyServer: JettyServer = JettyServer(),
    private val watcher: BakeWatcher = BakeWatcher()
) {
    var jBakeConfigurationFactory: JBakeConfigurationFactory?
    /**
     * Optional constructor to externalize dependencies.
     *
     * @param baker   A [Baker] instance
     * @param jettyServer   A [JettyServer] instance
     * @param watcher A [BakeWatcher] instance
     */
    /**
     * Default constructor.
     */
    init {
        this.jBakeConfigurationFactory = JBakeConfigurationFactory()
    }

    @Throws(JBakeException::class)
    fun run(args: Array<String>) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger()
            SLF4JBridgeHandler.install()

            val config: JBakeConfiguration

            val res = parseArguments(args)
            if (res.isRunServer) {
                config = this.jBakeConfigurationFactory!!.setEncoding(res.propertiesEncoding)
                    .createJettyJbakeConfiguration(
                        res.getSource(),
                        res.getDestination(),
                        res.getConfig(),
                        res.isClearCache
                    )
            } else {
                config = this.jBakeConfigurationFactory!!.setEncoding(res.propertiesEncoding)
                    .createDefaultJbakeConfiguration(
                        res.getSource(),
                        res.getDestination(),
                        res.getConfig(),
                        res.isClearCache
                    )
            }
            run(res, config)
        } catch (e: JBakeException) {
            throw e
        } catch (mex: CommandLine.MissingParameterException) {
            throw JBakeException(SystemExit.CONFIGURATION_ERROR, mex.message, mex)
        } catch (e: Throwable) {
            throw JBakeException(SystemExit.ERROR, "An unexpected error occurred: " + e.message, e)
        }
    }

    fun run(res: LaunchOptions, config: JBakeConfiguration) {
        println("JBake " + config.version + " (" + config.buildTimeStamp + " " + config.abbreviatedGitHash + "#) [http://jbake.org]")
        println()

        if (res.isHelpNeeded) {
            printUsage(res)
            // Help was requested, so we are done here
            return
        }

        if (res.isListConfig) {
            val printer = ConfigurationPrinter(config, System.out)
            printer.print()
            return
        }

        if (res.isBake) {
            baker.bake(config)
        }

        if (res.isInit) {
            initStructure(res.template, config)
        }

        if (res.isRunServer) {
            watcher.start(config)
            // TODO: short term fix until bake, server, init commands no longer share underlying values (such as source/dest)
            if (res.isBake) {
                // bake and server commands have been run together
                if (res.getDestination() != null) {
                    // use the destination provided via the commandline
                    runServer(res.getDestination(), config)
                } else if (res.getSource().getPath() != ".") {
                    // use the source folder provided via the commandline
                    runServer(res.getSource(), config)
                } else {
                    // use the default DESTINATION_FOLDER value
                    runServer(config.destinationFolder!!, config)
                }
            } else {
                // use the default destination folder
                runServer(config.destinationFolder!!, config)
            }
        }
    }

    private fun parseArguments(args: Array<String>): LaunchOptions {
        return CommandLine.populateCommand<LaunchOptions>(LaunchOptions(), *args)
    }

    private fun printUsage(options: Any) {
        val cli = CommandLine(options)
        cli.setUsageHelpLongOptionsMaxWidth(28)
        cli.usage(System.out)
    }

    private fun runServer(path: File, configuration: JBakeConfiguration) {
        jettyServer.run(path.getPath(), configuration)
    }

    private fun initStructure(type: String?, config: JBakeConfiguration) {
        val init = Init(config)
        try {
            val templateFolder = FileUtil.runningLocation
            val outputFolder: File
            if (config.sourceFolder != null) {
                outputFolder = config.sourceFolder!!
            } else {
                outputFolder = File(".")
            }
            init.run(outputFolder, templateFolder, type)
            println("Base folder structure successfully created.")
        } catch (e: Exception) {
            val msg = "Failed to initialise structure: " + e.message
            throw JBakeException(SystemExit.INIT_ERROR, msg, e)
        }
    }


    companion object {
        private const val USAGE_PREFIX = "Usage: jbake"
        private const val ALT_USAGE_PREFIX = "   or  jbake"
        private val logger: Logger = LoggerFactory.getLogger("jbake")

        /**
         * Runs the app with the given arguments.
         *
         * @param args Application arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                Main().run(args)
            } catch (e: JBakeException) {
                logger.error(e.message)
                logger.trace(e.message, e)
                if (e.cause is CommandLine.MissingParameterException) {
                    printUsage()
                }
                System.exit(e.getExit())
            }
        }

        fun printUsage() {
            CommandLine.usage(LaunchOptions(), System.out)
        }
    }
}
