package org.jbake.gradle.impl

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Initialises sample folder structure with pre-defined template.
 * Modified from {@code org.jbake.launcher.Init} original by Jonathan Bullock
 */
class Init {
    def config

    /**
     * Performs checks on output folder before extracting template file
     *
     * @param outputFolder Target directory for extracting template file
     * @param templateType Type of the template to be used
     */
    void run(File outputFolder, String templateType) {
        String templateFileName = "jbake-example-project-$templateType"
        URL url = "https://github.com/jbake-org/${templateFileName}/archive/master.zip".toURL()
        run(outputFolder, url)
    }

    /**
     * Performs checks on output folder before extracting template file
     *
     * @param outputFolder Target directory for extracting template file
     * @param templateUrl URL of the template to be used
     */
    void run(File outputFolder, URL url) {
        if (!outputFolder.canWrite()) {
            throw new IllegalStateException("Output folder is not writeable!")
        }

        File[] contents = outputFolder.listFiles()
        boolean safe = true
        if (contents != null) {
            for (File content : contents) {
                if (content.isDirectory()) {
                    if (content.getName().equalsIgnoreCase(config.getString("template.folder"))) {
                        safe = false
                    }
                    if (content.getName().equalsIgnoreCase(config.getString("content.folder"))) {
                        safe = false
                    }
                    if (content.getName().equalsIgnoreCase(config.getString("asset.folder"))) {
                        safe = false
                    }
                }
            }
        }

        if (!safe) {
            throw new IllegalStateException(String.format("Output folder '%s' already contains structure!",
                outputFolder.getAbsolutePath()))
        }

        File tmpZipFile = File.createTempFile('jbake-template-', '')
        tmpZipFile.withOutputStream { it.write(url.bytes) }
        File tmpOutput = File.createTempDir('jbake-extracted-', '')
        extract(new FileInputStream(tmpZipFile), tmpOutput)

        if (tmpOutput.listFiles().size()) {
            tmpOutput.listFiles()[0].renameTo(outputFolder)
        } else {
            tmpOutput.renameTo(outputFolder)
        }
    }

    /**
     * Extracts content of Zip file to specified output path.
     *
     * @param is {@link InputStream} InputStream of Zip file
     * @param outputFolder folder where Zip file should be extracted to
     * @throws IOException    if IOException occurs
     */
    static void extract(InputStream is, File outputFolder) throws IOException {
        ZipInputStream zis = new ZipInputStream(is)
        ZipEntry entry
        byte[] buffer = new byte[1024]

        while ((entry = zis.getNextEntry()) != null) {
            File outputFile = new File(outputFolder.getCanonicalPath() + File.separatorChar + entry.getName())
            File outputParent = new File(outputFile.getParent())
            outputParent.mkdirs()

            if (entry.isDirectory()) {
                if (!outputFile.exists()) {
                    outputFile.mkdir()
                }
            } else {
                FileOutputStream fos = new FileOutputStream(outputFile)

                int len
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len)
                }

                fos.close()
            }
        }
    }
}