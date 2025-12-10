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
        var somethingAlreadyExists = false
        if (contents != null) {
            for (content in contents) {
                if (!content.isDirectory()) continue
                if (content.getName().equals(config.templateDirName, ignoreCase = true)) somethingAlreadyExists = true
                if (content.getName().equals(config.contentDirName, ignoreCase = true)) somethingAlreadyExists = true
                if (content.getName().equals(config.assetDirName, ignoreCase = true)) somethingAlreadyExists = true
            }
        }

        if (somethingAlreadyExists) throw Exception("Output dir '${outputDir.absolutePath}' already contains structure!")

        val exampleProjectName = config.getExampleProjectByType(templateType)
            ?: throw Exception("No known example project for template type '$templateType'.")

        // Try loading from classpath first (when running from jar)
        // TODO: Handle exceptions
        val resourceStream = javaClass.classLoader.getResourceAsStream(exampleProjectName)
        if (resourceStream != null) {
            ZipUtil.extract(resourceStream, outputDir)
            return
        }

        // Fall back to filesystem (development mode)
        val templateFile = templateDir.resolve(exampleProjectName)
        if (!templateFile.exists())
            throw Exception("Cannot find example project file: $exampleProjectName (tried classpath and filesystem at ${templateFile.path})")

        // TODO: Handle exceptions
        // TODO: Document what these files are supposed to be and how to build them.
        ZipUtil.extract(FileInputStream(templateFile), outputDir)
    }
}
