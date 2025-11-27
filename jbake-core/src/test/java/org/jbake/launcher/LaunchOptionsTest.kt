package org.jbake.launcher

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.jbake.app.configuration.ConfigUtil
import org.jbake.util.PathUtils.SYSPROP_USER_DIR
import picocli.CommandLine
import java.io.File

class LaunchOptionsTest : StringSpec({

    fun parseArgs(args: Array<String>): LaunchOptions {
        return CommandLine.populateCommand(LaunchOptions(), *args)
    }

    "showHelp" {
        val args = arrayOf("-h")
        val res = parseArgs(args)
        res.isHelpNeeded shouldBe true
    }

    "runServer" {
        val args = arrayOf("-s")
        val res = parseArgs(args)

        res.isRunServer shouldBe true
    }

    "runServerWithFolder" {
        val args = arrayOf("-s", "/tmp")
        val res = parseArgs(args)

        res.isRunServer shouldBe true
        res.getSource() shouldBe File("/tmp")
    }

    "init" {
        val args = arrayOf("-i")
        val res = parseArgs(args)

        res.isInit shouldBe true
        res.template shouldBe "freemarker"
    }

    "initWithTemplate" {
        val args = arrayOf("-i", "-t", "foo")
        val res = parseArgs(args)

        res.isInit shouldBe true
        res.template shouldBe "foo"
    }

    "initWithSourceDirectory" {
        val args = arrayOf("-i", "/tmp")
        val res = parseArgs(args)

        res.isInit shouldBe true
        res.sourceValue shouldBe "/tmp"
    }

    "initWithTemplateAndSourceDirectory" {
        val args = arrayOf("-i", "-t", "foo", "/tmp")
        val res = parseArgs(args)

        res.isInit shouldBe true
        res.template shouldBe "foo"
        res.sourceValue shouldBe "/tmp"
    }

    "shouldThrowAnExceptionCallingTemplateWithoutInitOption" {
        val args = arrayOf("-t", "groovy-mte")

        shouldThrow<CommandLine.MissingParameterException> {
            parseArgs(args)
        }
    }

    "bake" {
        val args = arrayOf("-b")
        val res = parseArgs(args)

        res.isBake shouldBe true
    }

    "listConfig" {
        val args = arrayOf("-ls")
        val res = parseArgs(args)

        res.isListConfig shouldBe true
    }

    "listConfigLongOption" {
        val args = arrayOf("--list-settings")
        val res = parseArgs(args)

        res.isListConfig shouldBe true
    }

    "customPropertiesEncoding" {
        val args = arrayOf("--prop-encoding", "utf-16")
        val res = parseArgs(args)

        res.propertiesEncoding shouldBe "utf-16"
    }

    "defaultEncodingIsUtf8" {
        val args = arrayOf<String>()
        val res = parseArgs(args)

        res.propertiesEncoding shouldBe "utf-8"
    }

    "bakeNoArgs" {
        val args = arrayOf<String>()
        val res = parseArgs(args)

        res.isHelpNeeded shouldBe true
        res.isRunServer shouldBe false
        res.isInit shouldBe false
        res.isBake shouldBe false
        res.getSource().path shouldBe SYSPROP_USER_DIR
        res.getDestination().path shouldBe File(SYSPROP_USER_DIR, "output").path
        res.getConfig().path shouldBe File(SYSPROP_USER_DIR, ConfigUtil.CONFIG_FILE).path
    }

    "bakeWithArgs" {
        val args = arrayOf("/tmp/source", "/tmp/destination")
        val res = parseArgs(args)

        res.isHelpNeeded shouldBe false
        res.isRunServer shouldBe false
        res.isInit shouldBe false
        res.isBake shouldBe true
        res.getSource() shouldBe File("/tmp/source")
        res.getDestination() shouldBe File("/tmp/destination")
    }

    "configArg" {
        val args = arrayOf("-c", "foo")
        val res = parseArgs(args)
        res.getConfig().absoluteFile.toString() shouldBe File(SYSPROP_USER_DIR, "foo").absoluteFile.toString()
    }
})
