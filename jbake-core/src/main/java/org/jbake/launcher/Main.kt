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
import kotlin.system.exitProcess

/**
 * Launcher for JBake.
 */
class Main @JvmOverloads constructor(
    private val baker: Baker = Baker(),
    private val jettyServer: JettyServer = JettyServer(),
    private val watcher: BakeWatcher = BakeWatcher()
) {
    var jBakeConfigurationFactory: JBakeConfigurationFactory = JBakeConfigurationFactory()

    @Throws(JBakeException::class)
    fun run(args: Array<String>) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger()
            SLF4JBridgeHandler.install()

            val config: JBakeConfiguration

            val arguments = parseArguments(args)
            if (arguments.isRunServer) {
                config = this.jBakeConfigurationFactory.setEncoding(arguments.propertiesEncoding)
                    .createJettyJbakeConfiguration(
                        arguments.getSource(),
                        arguments.getDestination(),
                        arguments.getConfig(),
                        arguments.isClearCache
                    )
            } else {
                config = this.jBakeConfigurationFactory.setEncoding(arguments.propertiesEncoding)
                    .createDefaultJbakeConfiguration(
                        arguments.getSource(),
                        arguments.getDestination(),
                        arguments.getConfig(),
                        arguments.isClearCache
                    )
            }
            run(arguments, config)
        } catch (e: JBakeException) {
            throw e
        } catch (mex: CommandLine.MissingParameterException) {
            logger.error(mex.message)
            throw JBakeException(SystemExit.CONFIGURATION_ERROR, mex.message, mex)
        } catch (e: Throwable) {
            throw JBakeException(SystemExit.ERROR, "An unexpected error occurred: " + e.message, e)
        }
    }

    fun run(launchOptions: LaunchOptions, config: JBakeConfiguration) {
        println("JBake " + config.version + " (" + config.buildTimeStamp + " " + config.abbreviatedGitHash + "#) [http://jbake.org]")
        println()

        if (launchOptions.isHelpNeeded) {
            printUsage(launchOptions)
            // Help was requested, so we are done here.
            return
        }

        if (launchOptions.isListConfig) {
            val printer = ConfigurationPrinter(config, System.out)
            printer.print()
            return
        }

        if (launchOptions.isBake)
            baker.bake(config)

        if (launchOptions.isInit)
            initStructure(launchOptions.template!!, config)

        if (launchOptions.isRunServer) {
            watcher.start(config)

            // TODO: Short term fix until bake, server, init commands no longer share underlying values (such as source/dest).
            if (!launchOptions.isBake) {
                // Use the default destination folder.
                runServer(config.destinationFolder, config)
            }
            else {
                // Bake and server commands have been run together.
                when {
                    launchOptions.getDestination() != null ->
                        // Use the destination provided via the commandline.
                        runServer(launchOptions.getDestination(), config)
                    launchOptions.getSource().path != "." ->
                        // Use the source folder provided via the commandline.
                        runServer(launchOptions.getSource(), config)
                    else ->
                        // Use the default DESTINATION_FOLDER value.
                        runServer(config.destinationFolder, config)
                }
            }
        }
    }

    private fun parseArguments(args: Array<String>): LaunchOptions {
        return CommandLine.populateCommand(LaunchOptions(), *args)
    }

    private fun printUsage(options: Any) {
        val cli = CommandLine(options)
        cli.usageHelpLongOptionsMaxWidth = 28
        cli.usage(System.out)
    }

    private fun runServer(path: File, configuration: JBakeConfiguration) {
        jettyServer.run(path.path, configuration)
    }

    private fun initStructure(type: String, config: JBakeConfiguration) {
        val init = Init(config)
        try {
            val templateFolder = FileUtil.runningLocation
            val outputFolder = if (config.sourceFolder != null) config.sourceFolder!!
                else File(".")

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
                exitProcess(e.getExit())
            }
        }

        fun printUsage() {
            CommandLine.usage(LaunchOptions(), System.out)
        }
    }
}
