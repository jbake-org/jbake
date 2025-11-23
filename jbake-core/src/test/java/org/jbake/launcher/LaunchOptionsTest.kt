package org.jbake.launcher

import org.assertj.core.api.Assertions
import org.jbake.app.configuration.ConfigUtil
import org.jbake.util.PathConstants
import org.jbake.util.PathConstants.fS
import org.junit.Test
import picocli.CommandLine
import java.io.File

class LaunchOptionsTest {
    @Test
    fun showHelp() {
        val args = arrayOf("-h")
        val res = parseArgs(args)
        Assertions.assertThat(res.isHelpNeeded).isTrue()
    }

    @Test
    fun runServer() {
        val args = arrayOf("-s")
        val res = parseArgs(args)

        Assertions.assertThat(res.isRunServer).isTrue()
    }

    @Test
    fun runServerWithFolder() {
        val args = arrayOf("-s", "/tmp")
        val res = parseArgs(args)

        Assertions.assertThat(res.isRunServer).isTrue()
        Assertions.assertThat(res.getSource()).isEqualTo(File("/tmp"))
    }

    @Test
    fun init() {
        val args = arrayOf("-i")
        val res = parseArgs(args)

        Assertions.assertThat(res.isInit).isTrue()
        Assertions.assertThat(res.template).isEqualTo("freemarker")
    }

    @Test
    fun initWithTemplate() {
        val args = arrayOf("-i", "-t", "foo")
        val res = parseArgs(args)

        Assertions.assertThat(res.isInit).isTrue()
        Assertions.assertThat(res.template).isEqualTo("foo")
    }

    @Test
    fun initWithSourceDirectory() {
        val args = arrayOf("-i", "/tmp")
        val res = parseArgs(args)

        Assertions.assertThat(res.isInit).isTrue()
        Assertions.assertThat(res.sourceValue).isEqualTo("/tmp")
    }

    @Test
    fun initWithTemplateAndSourceDirectory() {
        val args = arrayOf("-i", "-t", "foo", "/tmp")
        val res = parseArgs(args)

        Assertions.assertThat(res.isInit).isTrue()
        Assertions.assertThat(res.template).isEqualTo("foo")
        Assertions.assertThat(res.sourceValue).isEqualTo("/tmp")
    }

    @Test
    fun shouldThrowAnExceptionCallingTemplateWithoutInitOption() {
        val args = arrayOf("-t", "groovy-mte")

        Assertions.assertThatExceptionOfType(CommandLine.MissingParameterException::class.java)
            .isThrownBy {
                parseArgs(args)
            }.withMessage("Error: Missing required argument(s): --init")
    }

    @Test
    fun bake() {
        val args = arrayOf("-b")
        val res = parseArgs(args)

        Assertions.assertThat(res.isBake).isTrue()
    }

    @Test
    fun listConfig() {
        val args = arrayOf("-ls")
        val res = parseArgs(args)

        Assertions.assertThat(res.isListConfig).isTrue()
    }

    @Test
    fun listConfigLongOption() {
        val args = arrayOf("--list-settings")
        val res = parseArgs(args)

        Assertions.assertThat(res.isListConfig).isTrue()
    }

    @Test
    fun customPropertiesEncoding() {
        val args = arrayOf("--prop-encoding", "utf-16")
        val res = parseArgs(args)

        Assertions.assertThat(res.propertiesEncoding).isEqualTo("utf-16")
    }

    @Test
    fun defaultEncodingIsUtf8() {
        val args = arrayOf<String>()
        val res = parseArgs(args)

        Assertions.assertThat(res.propertiesEncoding).isEqualTo("utf-8")
    }

    @Test
    fun bakeNoArgs() {
        val args = arrayOf<String>()
        val res = parseArgs(args)

        Assertions.assertThat(res.isHelpNeeded).isTrue()
        Assertions.assertThat(res.isRunServer).isFalse()
        Assertions.assertThat(res.isInit).isFalse()
        Assertions.assertThat(res.isBake).isFalse()
        Assertions.assertThat(res.getSource().path).isEqualTo(System.getProperty("user.dir"))
        Assertions.assertThat(res.getDestination().path)
            .isEqualTo(System.getProperty("user.dir") + fS + "output")
        Assertions.assertThat(res.getConfig().path)
            .isEqualTo(System.getProperty("user.dir") + fS + ConfigUtil.CONFIG_FILE)
    }

    @Test
    fun bakeWithArgs() {
        val args = arrayOf("/tmp/source", "/tmp/destination")
        val res = parseArgs(args)

        Assertions.assertThat(res.isHelpNeeded).isFalse()
        Assertions.assertThat(res.isRunServer).isFalse()
        Assertions.assertThat(res.isInit).isFalse()
        Assertions.assertThat(res.isBake).isTrue()
        Assertions.assertThat(res.getSource()).isEqualTo(File("/tmp/source"))
        Assertions.assertThat(res.getDestination()).isEqualTo(File("/tmp/destination"))
    }

    @Test
    fun configArg() {
        val args = arrayOf("-c", "foo")
        val res = parseArgs(args)
        Assertions.assertThat(res.getConfig().getAbsoluteFile().toString())
            .isEqualTo(System.getProperty("user.dir") + fS + "foo")
    }

    private fun parseArgs(args: Array<String>): LaunchOptions {
        return CommandLine.populateCommand(LaunchOptions(), *args)
    }
}
