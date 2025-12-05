package org.jbake.launcher

import org.jbake.app.FileUtil
import org.jbake.app.JBakeExitException
import org.jbake.app.SystemExit
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

    @Throws(JBakeExitException::class)
    fun run(arguments: Array<String>) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger()
            SLF4JBridgeHandler.install()

            val config: JBakeConfiguration

            val args: LaunchOptions = parseArguments(arguments)
            config = this.jBakeConfigurationFactory
                .setEncoding(args.propertiesEncoding)
                .let {
                    if (args.isRunServer)
                        it.createJettyJbakeConfiguration(args.getSource(), args.getDestination(), args.getConfig(), args.isClearCache)
                    else
                        it.createDefaultJbakeConfiguration(args.getSource(), args.getDestination(), args.getConfig(), args.isClearCache)
                }

            run(args, config)
        }
        catch (e: JBakeExitException) { throw e }
        catch (mex: CommandLine.MissingParameterException) {
            log.error(mex.message)
            throw JBakeExitException(SystemExit.CONFIG_ERROR, mex.message ?: mex.javaClass.simpleName, mex)
        }
        catch (e: Throwable) {
            throw JBakeExitException(SystemExit.ERROR, "${e.javaClass.simpleName}: An unexpected error occurred: " + e.message, e)
        }
    }

    fun run(launchOptions: LaunchOptions, config: JBakeConfiguration) {
        println("JBake " + config.jbakeVersion + " (" + config.buildTimeStamp + " " + config.abbreviatedGitHash + "#) [http://jbake.org]")
        println()

        if (launchOptions.isHelpNeeded) {
            printUsage(launchOptions)
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
            val serverPath = when {
                !launchOptions.isBake -> config.destinationDir // Use the default destination directory.
                launchOptions.getDestination() != null -> launchOptions.getDestination() // Use the destination provided via the commandline.
                launchOptions.getSource().path != "." -> launchOptions.getSource() // Use the source directory provided via the commandline.
                else -> config.destinationDir // Use the default DESTINATION_FOLDER value.
            }
            runServer(serverPath, config)
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
            val templateDir = FileUtil.runningLocation
            val outputDir = config.sourceDir ?: File(".")

            init.run(outputDir, templateDir, type)
            println("Base dir structure successfully created.")
        } catch (e: Exception) {
            val msg = "Failed to initialise structure: " + e.message
            throw JBakeExitException(SystemExit.INIT_ERROR, msg, e)
        }
    }


    companion object {
        private const val USAGE_PREFIX = "Usage: jbake"
        private const val ALT_USAGE_PREFIX = "   or  jbake"

        /** Runs the app with the given arguments. */
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                Main().run(args)
            } catch (e: JBakeExitException) {
                log.error(e.message)
                log.debug(e.message, e)
                if (e.cause is CommandLine.MissingParameterException) printUsage()
                exitProcess(e.getExit())
            }
            catch (e: Throwable) {
                log.error(e.message, e)
            }
        }

        fun printUsage() = CommandLine.usage(LaunchOptions(), System.out)

        private val log: Logger = LoggerFactory.getLogger("Main.kt")
    }
}
