package org.jbake.launcher

import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.jbake.app.configuration.ConfigUtil
import org.junit.Test
import picocli.CommandLine
import java.io.File

class LaunchOptionsTest {
    @Test
    fun showHelp() {
        val args = arrayOf<String>("-h")
        val res = parseArgs(args)
        Assertions.assertThat(res.isHelpNeeded).isTrue()
    }

    @Test
    fun runServer() {
        val args = arrayOf<String>("-s")
        val res = parseArgs(args)

        Assertions.assertThat(res.isRunServer).isTrue()
    }

    @Test
    fun runServerWithFolder() {
        val args = arrayOf<String>("-s", "/tmp")
        val res = parseArgs(args)

        Assertions.assertThat(res.isRunServer).isTrue()
        Assertions.assertThat(res.getSource()).isEqualTo(File("/tmp"))
    }

    @Test
    fun init() {
        val args = arrayOf<String>("-i")
        val res = parseArgs(args)

        Assertions.assertThat(res.isInit).isTrue()
        Assertions.assertThat(res.template).isEqualTo("freemarker")
    }

    @Test
    fun initWithTemplate() {
        val args = arrayOf<String>("-i", "-t", "foo")
        val res = parseArgs(args)

        Assertions.assertThat(res.isInit).isTrue()
        Assertions.assertThat(res.template).isEqualTo("foo")
    }

    @Test
    fun initWithSourceDirectory() {
        val args = arrayOf<String>("-i", "/tmp")
        val res = parseArgs(args)

        Assertions.assertThat(res.isInit).isTrue()
        Assertions.assertThat(res.sourceValue).isEqualTo("/tmp")
    }

    @Test
    fun initWithTemplateAndSourceDirectory() {
        val args = arrayOf<String>("-i", "-t", "foo", "/tmp")
        val res = parseArgs(args)

        Assertions.assertThat(res.isInit).isTrue()
        Assertions.assertThat(res.template).isEqualTo("foo")
        Assertions.assertThat(res.sourceValue).isEqualTo("/tmp")
    }

    @Test
    fun shouldThrowAnExceptionCallingTemplateWithoutInitOption() {
        val args = arrayOf<String>("-t", "groovy-mte")

        Assertions.assertThatExceptionOfType<CommandLine.MissingParameterException?>(CommandLine.MissingParameterException::class.java)
            .isThrownBy(ThrowingCallable {
                val res = parseArgs(args)
            }).withMessage("Error: Missing required argument(s): --init")
    }

    @Test
    fun bake() {
        val args = arrayOf<String>("-b")
        val res = parseArgs(args)

        Assertions.assertThat(res.isBake).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun listConfig() {
        val args = arrayOf<String>("-ls")
        val res = parseArgs(args)

        Assertions.assertThat(res.isListConfig).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun listConfigLongOption() {
        val args = arrayOf<String>("--list-settings")
        val res = parseArgs(args)

        Assertions.assertThat(res.isListConfig).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun customPropertiesEncoding() {
        val args = arrayOf<String>("--prop-encoding", "utf-16")
        val res = parseArgs(args)

        Assertions.assertThat(res.propertiesEncoding).isEqualTo("utf-16")
    }

    @Test
    @Throws(Exception::class)
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
        Assertions.assertThat(res.getSource().getPath()).isEqualTo(System.getProperty("user.dir"))
        Assertions.assertThat(res.getDestination().getPath())
            .isEqualTo(System.getProperty("user.dir") + File.separator + "output")
        Assertions.assertThat(res.getConfig().getPath())
            .isEqualTo(System.getProperty("user.dir") + File.separator + ConfigUtil.CONFIG_FILE)
    }

    @Test
    fun bakeWithArgs() {
        val args = arrayOf<String>("/tmp/source", "/tmp/destination")
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
        val args = arrayOf<String>("-c", "foo")
        val res = parseArgs(args)
        Assertions.assertThat(res.getConfig().getAbsoluteFile().toString())
            .isEqualTo(System.getProperty("user.dir") + File.separator + "foo")
    }

    private fun parseArgs(args: Array<String>): LaunchOptions {
        return CommandLine.populateCommand<LaunchOptions>(LaunchOptions(), *args)
    }
}
