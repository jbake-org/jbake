package org.jbake.launcher

import org.jbake.app.ZipUtil
import org.jbake.app.configuration.JBakeConfiguration
import java.io.File
import java.io.FileInputStream

/**
 * Initialises sample directory structure with pre-defined template
 */
class Init(private val config: JBakeConfiguration) {

    /**
     * Performs checks on output directory before extracting template file.
     *
     * @throws Exception if required directory structure can't be achieved without content overwriting.
     */
    fun run(outputDir: File, templateDir: File, templateType: String) {
        if (!outputDir.canWrite()) throw Exception("Output dir is not writeable!")

        val contents = outputDir.listFiles()
        var safe = true
        if (contents != null) {
            for (content in contents) {
                if (!content.isDirectory()) continue
                if (content.getName().equals(config.templateDirName, ignoreCase = true)) safe = false
                if (content.getName().equals(config.contentDirName, ignoreCase = true)) safe = false
                if (content.getName().equals(config.assetDirName, ignoreCase = true)) safe = false
            }
        }

        if (!safe) throw Exception(String.format("Output dir '%s' already contains structure!", outputDir.absolutePath))

        val exampleProjectName = config.getExampleProjectByType(templateType)
            ?: throw Exception("No name of the example project for template type $templateType")

        // Try loading from classpath first (when running from jar)
        val resourceStream = javaClass.classLoader.getResourceAsStream(exampleProjectName)
        if (resourceStream != null) {
            ZipUtil.extract(resourceStream, outputDir)
            return
        }

        // Fall back to filesystem (development mode)
        val templateFile = File(templateDir, exampleProjectName)
        if (!templateFile.exists())
            throw Exception("Cannot find example project file: $exampleProjectName (tried classpath and filesystem at ${templateFile.path})")

        ZipUtil.extract(FileInputStream(templateFile), outputDir)
    }
}
